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
#include <taglib/mpegfile.h>
#include <taglib/vorbisfile.h>
#include <taglib/flacfile.h>



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

    long count = db.getTrackCount();
    stats->setFound(count);
    DbPathIterator iter = db.getAllPaths();

    db.begin(); // turn off autocommit
    while(iter.next()) {
        QString path = QString::fromUtf8(iter.getPath());
        QString ext = path.right(path.length() - path.lastIndexOf(".") - 1);
        //out << "Now processing: " << path << endl;
        //out << "File type: " << ext << endl;

        ext = ext.toLower();

        // simple and stupid detection of file-type by extension
        if (ext == QString("mp3")) {
            // MPEG file
            QByteArray ba = path.toUtf8();
            MPEG::File *file = new MPEG::File(ba.data());
            QByteArray hash = extractImageID3(file->ID3v2Tag(), &db);
            if (!hash.isNull()) {
                // set cover picture
                db.setCover(hash.data(), path.toUtf8().data());
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
            } else {
                hash = extractImageOgg(file->xiphComment(), &db); 
                if (!hash.isNull()) {
                    db.setCover(hash.data(), path.toUtf8().data());
                }
            }
            delete file;
        }
        stats->incrementProcessed();
    }
    db.commit();
}

QByteArray Covers::extractImageID3(ID3v2::Tag *tag, Database *db)
{
    if (!tag) {
        return QByteArray();
    }

    if (tag->isEmpty()) {
        return QByteArray();
    }

    // get APIC (attached picture) frames
    ID3v2::FrameList frames = tag->frameList("APIC");
    if(frames.isEmpty()) {
        return QByteArray();
    }
    // pick the first item from the list and use it as picture
    ID3v2::AttachedPictureFrame *picture =
        static_cast<ID3v2::AttachedPictureFrame *>(frames.front());

    return storeID3PictureFrame(picture, db);
}

QByteArray Covers::extractImageOgg(Ogg::XiphComment *tag, Database *db)
{
    if (!tag) {
        return QByteArray();
    }

    if (tag->isEmpty()) {
        return QByteArray();
    }

    StringList pictureBlock = tag->fieldListMap()["METADATA_BLOCK_PICTURE"];
    if (!pictureBlock.isEmpty()) {
        String base64 = pictureBlock.front();
        QByteArray frameData = QByteArray::fromBase64(QByteArray(base64.toCString()));
        ID3v2::AttachedPictureFrame picture(ByteVector(frameData.data(), frameData.size()));
        return storeID3PictureFrame(&picture, db);
    }
    
    return QByteArray();
}

QByteArray Covers::storeID3PictureFrame(ID3v2::AttachedPictureFrame* picture, Database* db)
{
    String mimetype = picture->mimeType();
    ByteVector pictureData = picture->picture();
    int length = pictureData.size();
    QByteArray md5Data = md5(pictureData.data(), length);
    md5Data = md5Data.toHex(); // convert to hex representation


    if (!db->hasCover(md5Data.data())) {
        db->addCover(md5Data.data(), pictureData.data(), length, mimetype.toCString());
        //out << endl << "Added new cover image: " << md5Data << endl;
    }
    return md5Data;
}


QByteArray Covers::md5(char* input, int length)
{
    QByteArray data = QByteArray::fromRawData(input, length);

    return QCryptographicHash::hash(data,QCryptographicHash::Md5);
}

#include "Covers.moc"
