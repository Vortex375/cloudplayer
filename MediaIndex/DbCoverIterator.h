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


#ifndef DBCOVERITERATOR_H
#define DBCOVERITERATOR_H
#include <sqlite3.h>
#include <QtCore/QByteArray>
#include <QtCore/QString>

class DbCoverIterator
{

public:
    DbCoverIterator(sqlite3_stmt *stmt);
    virtual ~DbCoverIterator();
    bool next();
    QString getHash();
    QString getMimeType();
    QByteArray getCover();
private:
    sqlite3_stmt *stmt;
};

#endif // DBCOVERITERATOR_H
