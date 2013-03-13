/*
    <one line to give the program's name and a brief idea of what it does.>
    Copyright (C) 2012  Benjamin Schmitz <benni@wolpzone.de>

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/


#include "Covers.h"
#include <QtCore/QTextStream>
#include <QtCore/QCryptographicHash>
#include <QtCore/QFile>
#include <QtCore/QDir>
#include <QtCore/QDebug>
#include <taglib/mpegfile.h>
#include <taglib/vorbisfile.h>
#include <taglib/flacfile.h>
#include "string.h"


using namespace TagLib;

static QTextStream out(stdout);

Covers::Covers(Stats *s, char *dbPath)
{
    this->stats = s;
    this->dbPath = dbPath;
}

Covers::~Covers()
{

}

void Covers::run()
{
    // open database
    Database db;
    if (!db.open(dbPath)) {
        out << "Error: failed to open database." << endl;
        exit(1);
        return;
    };

    long count = db.getMissingCoverCount();
    stats->setFound(count);
    DbPathIterator iter = db.getMissingCovers();

    db.begin(); // turn off autocommit
    while(iter.next()) {
        QString path = QString::fromUtf8(iter.getPath());
        QString ext = path.right(path.length() - path.lastIndexOf(".") - 1);
        //qDebug() << "Now processing: " << path << endl;
        //qDebug()  << "File type: " << ext << endl;

        ext = ext.toLower();
        bool foundCover = false;

        // simple and stupid detection of file-type by extension
        // first, try to find the cover in the metadata of the file
        if (ext == QString("mp3")) {
            // MPEG file
            QByteArray ba = path.toUtf8();
            MPEG::File *file = new MPEG::File(ba.data());
            QByteArray hash = extractImageID3(file->ID3v2Tag(), &db);
            if (!hash.isNull()) {
                // set cover picture
                db.setCover(hash.data(), path.toUtf8().data());
                foundCover = true;
            }
            delete file;
        } else if (ext == QString("ogg")) {
            // Ogg file
            QByteArray ba = path.toUtf8();
            Ogg::Vorbis::File *file = new Ogg::Vorbis::File(ba.data());
            QByteArray hash = extractImageOgg(file->tag(), &db);
            if (!hash.isNull()) {
                // set cover picture
                db.setCover(hash.data(), path.toUtf8().data());
                foundCover = true;
            }
            delete file;
        } else if (ext == QString("flac")) {
            // FLAC files can have xiph comments or id3 tags
            QByteArray ba = path.toUtf8();
            FLAC::File *file = new FLAC::File(ba.data());
            QByteArray hash = extractImageID3(file->ID3v2Tag(), &db);
            if (!hash.isNull()) {
                // set cover picture
                db.setCover(hash.data(), path.toUtf8().data());
                foundCover = true;
            } else {
                hash = extractImageOgg(file->xiphComment(), &db);
                if (!hash.isNull()) {
                    db.setCover(hash.data(), path.toUtf8().data());
                    foundCover = true;
                }
            }
            delete file;
        }

        if (!foundCover) {
            // we haven't found a cover inside the file
            // search the file's directory instead
            QByteArray hash = extractImageFromDir(path, &db);

            if (!hash.isNull()) {
                db.setCover(hash.data(), path.toUtf8().data());
                foundCover = true;
            }
        }

        stats->incrementProcessed();
    }
    db.commit();
}

QByteArray Covers::extractImageID3(ID3v2::Tag *tag, Database *db)
{
//     qDebug() << "Extract image from ID3v2 tag." << endl;
    if (!tag) {
//         qDebug() << "No tag found." << endl;
        return QByteArray();
    }

    if (tag->isEmpty()) {
//         qDebug() << "No tag found" << endl;
        return QByteArray();
    }

    // get APIC (attached picture) frames
    ID3v2::FrameList frames = tag->frameList("APIC");
    if(frames.isEmpty()) {
//         qDebug() << "No picture frame found in tag" << endl;
        return QByteArray();
    }
    // pick the first item from the list and use it as picture
    ID3v2::AttachedPictureFrame *picture =
        static_cast<ID3v2::AttachedPictureFrame *>(frames.front());

    return storeID3PictureFrame(picture, db);
}

QByteArray Covers::extractImageOgg(Ogg::XiphComment *tag, Database *db)
{
//     qDebug() << "Extract image from Xiph comment" << endl;
    if (!tag) {
//         qDebug() << "No tag found" << endl;
        return QByteArray();
    }

    if (tag->isEmpty()) {
//         qDebug() << "No tag found" << endl;
        return QByteArray();
    }

    StringList pictureBlock = tag->fieldListMap()["METADATA_BLOCK_PICTURE"];
    if (!pictureBlock.isEmpty()) {
//         qDebug() << "No picture block found in tag" << endl;
        String base64 = pictureBlock.front();
        QByteArray frameData = QByteArray::fromBase64(QByteArray(base64.toCString()));
        ID3v2::AttachedPictureFrame picture(ByteVector(frameData.data(), frameData.size()));
        return storeID3PictureFrame(&picture, db);
    }

    return QByteArray();
}

QByteArray Covers::storeID3PictureFrame(ID3v2::AttachedPictureFrame* picture, Database* db)
{
//     qDebug() << "Store id3 picture frame." << endl;
    String mimetype = picture->mimeType();
    ByteVector pictureData = picture->picture();
    int length = pictureData.size();
    QByteArray md5Data = md5(pictureData.data(), length);
    md5Data = md5Data.toHex(); // convert to hex representation

//     qDebug() << "Got id3 picture: " << md5Data << endl;

    if (!db->hasCover(md5Data.data())) {
//         qDebug() << "Added new cover: " << md5Data << endl;
        db->addCover(md5Data.data(), pictureData.data(), length, mimetype.toCString());
        //out << endl << "Added new cover image: " << md5Data << endl;
    }
    return md5Data;
}

QByteArray Covers::extractImageFromDir(QString path, Database* db)
{
    QDir dir = QFileInfo(path).absoluteDir();
//     qDebug() << "Extract image from dir: " << dir.absolutePath() << endl;
    QStringList filter;
    filter << "*.jpg" << "*.png" << "*.gif";
    dir.setNameFilters(filter);
    QStringList files = dir.entryList();
    
    if (files.size() == 0) {
//         qDebug() << "No matching files found." << endl;
        return QByteArray();
    }
    
    int score[files.size()];

    // calculate a "score" for files based on their name
    // bonus points are received for the words "cover" and "front"
    for (int i = 0; i < files.size(); i++) {
      score[i] = 0; // initialize score
        if (files.at(i).contains("cover", Qt::CaseInsensitive)) {
            score[i]++;
        }
        if (files.at(i).contains("case", Qt::CaseInsensitive)) {
            score[i]++;
        }
        if (files.at(i).contains("front", Qt::CaseInsensitive)) {
            score[i]++;
        }
        if (files.at(i).contains("folder", Qt::CaseInsensitive)) {
            score[i]++;
        }
    }

    // find file with max score
    int max = 0;
    int pos = 0;
    for (int i = 0; i < files.size(); i++) {
        if (score[i] > max) {
            max = score[i];
            pos = i;
        }
    }

    // load and store file
    QFile imageFile(dir.absoluteFilePath(files.at(pos)));
//     qDebug() << "Picked cover file: " << imageFile.fileName() << endl;
    QString mimeType("");
    if (imageFile.fileName().endsWith("jpg", Qt::CaseInsensitive) || imageFile.fileName().endsWith("jpeg", Qt::CaseInsensitive)) {
        mimeType = QString("image/jpeg");
    } else if (imageFile.fileName().endsWith("png", Qt::CaseInsensitive)) {
        mimeType = QString("image/png");
    } else if (imageFile.fileName().endsWith("gif", Qt::CaseInsensitive)) {
        mimeType = QString("image/gif");
    }

    if (!imageFile.open(QIODevice::ReadOnly)) {
        out << "Unable to open file: " << imageFile.fileName() << endl;
    }
    QByteArray data = imageFile.readAll();
    QByteArray md5Data = md5(data.data(), data.size());
    md5Data = md5Data.toHex(); // convert to hex representation
    QByteArray mimeBa = mimeType.toAscii();
    imageFile.close();

//     qDebug() << "Got image file: " << md5Data << endl;
    
    if (!db->hasCover(md5Data.data())) {
//         qDebug() << "Added new cover: " << md5Data << endl;
        db->addCover(md5Data.data(), data.data(), data.size(), mimeBa.data());
    }

    return md5Data;
}


QByteArray Covers::md5(char* input, int length)
{
    QByteArray data = QByteArray::fromRawData(input, length);

    return QCryptographicHash::hash(data,QCryptographicHash::Md5);
}

#include "Covers.moc"
