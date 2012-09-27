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


#include "Indexer.h"
#include <boost/filesystem.hpp>
#include <taglib/fileref.h>
#include <taglib/tag.h>
#include <QDebug>
#include <QtCore/QTextStream>

using namespace boost::filesystem;

Indexer::Indexer(BlockingQueue *q, Stats *s, char *dbPath) : QThread() {
    this->queue = q;
    this->stats = s;
    this->dbPath = dbPath;
}


Indexer::~Indexer() {

}

void Indexer::run() {
    QTextStream out(stdout);
    // prepare database
    Database db;
    if (!db.open(dbPath)) {
        out << "Error: failed to open database." << endl;
        exit(1);
        return;
    };
    
    // create database tables
    if (!db.create()) {
        out << "Error: failed to initialize database." << endl;
        exit(1);
    }
    
    path p;
    db.begin();  // turn off autocommit
    while (!(p = queue->dequeue()).empty()) {
        TagLib::FileRef f(p.c_str());
        if (!f.isNull() && f.tag()) {
            db.insertTrack(
                f.tag()->title().toCString(true),
                f.tag()->artist().toCString(true),
                f.tag()->album().toCString(true),
                f.tag()->genre().toCString(true),
                f.tag()->track(),
                f.tag()->year(),
                p.c_str()
            );
        }
        stats->incrementProcessed();
    }
    db.clearMarks();
    db.commit(); // commit database
}


#include "Indexer.moc"