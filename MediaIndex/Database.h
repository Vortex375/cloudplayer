/*
    <one line to give the program's name and a brief idea of what it does.>
    Copyright (C) 2012  Benjamin Schmitz <vortex@wolpzone.de>

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


#ifndef DATABASE_H
#define DATABASE_H

#include <sqlite3.h>
#include "DbPathIterator.h"
#include "DbCoverIterator.h"

class Database
{

public:
    Database();
    virtual ~Database();
    bool open(char *path);
    bool create();
    void begin();
    void commit();
    void insertTrack(const char* title, const char* artist, const char* album, const char* genre, int track, int year, int duration, long fileSize, const char* path);
    void updateTrack(const char* title, const char* artist, const char* album, const char* genre, int track, int year, int duration, long fileSize, const char* path);
    void mark(sqlite_int64 id);
    void dropUnmarked();
    void clearMarks();
    sqlite3_int64 getLastModified(const char* path);
    void addCover(const char* md5, void* data, int length, const char* mimetype);
    bool hasCover(const char* md5);
    void setCover(const char* md5, const char* path);
    DbPathIterator getAllPaths();
    DbPathIterator getMissingCovers();
    sqlite3_int64 getTrackCount();
    sqlite3_int64 getCoverCount();
    sqlite3_int64 getMissingCoverCount();
    DbCoverIterator getAllCovers();
    
private:
    sqlite3 *db;
    sqlite3_stmt *insertTrackStmt;
    sqlite3_stmt *updateTrackStmt;
    sqlite3_stmt *getLastModifiedStmt;
    sqlite3_stmt *beginStmt;
    sqlite3_stmt *commitStmt;
    sqlite3_stmt *dropUnmarkedStmt;
    sqlite3_stmt *clearMarkStmt;
    sqlite3_stmt *markStmt;
    sqlite3_stmt *addCoverStmt;
    sqlite3_stmt *checkCoverStmt;
    sqlite3_stmt *setCoverStmt;
    sqlite3_stmt *countStmt;
    sqlite3_stmt *coverCountStmt;
    sqlite3_stmt *missingCoverCountStmt;
    void prepare();
    bool checkReturn(int ret);
};

#endif // DATABASE_H
