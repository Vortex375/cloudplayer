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


#include "Updater.h"
#include <taglib/fileref.h>
#include <taglib/tag.h>
#include <QtCore/QTextStream>
#include <QDebug>


Updater::Updater(BlockingQueue* q, Stats* s, char* dbPath): QThread()
{
    this->queue = q;
    this->stats = s;
    this->dbPath = dbPath;
}

Updater::~Updater()
{

}

void Updater::run()
{
    QTextStream out(stdout);
    // open database
    Database db;
    if (!db.open(dbPath)) {
        out << "Error: failed to open database." << endl;
        exit(1);
        return;
    };
    
    path p;
    db.begin();  // turn off autocommit
    while (!(p = queue->dequeue()).empty()) {
        long long lmod = db.getLastModified(p.c_str()); // getLastModified marks entry if found
        if (lmod < 0) {
            // new file
            TagLib::FileRef f(p.c_str());
            if (!f.isNull() && f.tag()) {
                db.insertTrack(
                    f.tag()->title().toCString(true),
                    f.tag()->artist().toCString(true),
                    f.tag()->album().toCString(true),
                    f.tag()->genre().toCString(true),
                    f.tag()->track(),
                    f.tag()->year(),
                    f.audioProperties()->length(), // duration in seconds
                    f.file()->length(), // file size in bytes (i think)
                    p.c_str()
                );
            }
        } else if (last_write_time(p) > lmod) {
            // file was modified since last indexed
            qDebug() << "File changed: " << p.c_str();
            TagLib::FileRef f(p.c_str());
            if (!f.isNull() && f.tag()) {
                db.updateTrack( // updateTrack marks entry if found
                    f.tag()->title().toCString(true),
                    f.tag()->artist().toCString(true),
                    f.tag()->album().toCString(true),
                    f.tag()->genre().toCString(true),
                    f.tag()->track(),
                    f.tag()->year(),
                    f.audioProperties()->length(), // duration in seconds
                    f.file()->length(), // file size in bytes (i think)
                    p.c_str()
                );
            }
        }
        stats->incrementProcessed();
    }
    db.dropUnmarked(); // remove deleted files from database
    db.clearMarks();
    db.commit(); // commit database
}

#include "Updater.moc"