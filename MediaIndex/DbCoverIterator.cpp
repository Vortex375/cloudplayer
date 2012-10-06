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


#include "DbCoverIterator.h"

DbCoverIterator::DbCoverIterator(sqlite3_stmt *stmt)
{
    this->stmt = stmt;
}

DbCoverIterator::~DbCoverIterator()
{
    sqlite3_finalize(stmt);
}

QString DbCoverIterator::getHash()
{
    return QString::fromAscii((char*) sqlite3_column_text(stmt, 0));
}

QString DbCoverIterator::getMimeType()
{
    return QString::fromAscii((char*) sqlite3_column_text(stmt, 3));
}


QByteArray DbCoverIterator::getCover()
{
    char *data = (char *) sqlite3_column_blob(stmt, 1);
    int length = sqlite3_column_int(stmt, 2);
    return QByteArray::fromRawData(data, length);
}

bool DbCoverIterator::next()
{
    return (sqlite3_step(stmt) == SQLITE_ROW);
}
