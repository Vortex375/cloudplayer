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


#ifndef COVERS_H
#define COVERS_H
#include "Stats.h"
#include "Database.h"
#include <QtCore/QThread>
#include <taglib/id3v2tag.h>
#include <taglib/xiphcomment.h>
#include <taglib/attachedpictureframe.h>

using namespace TagLib;

class Covers : public QThread
{
Q_OBJECT
public:
    Covers(Stats *s, char *dbPath);
    virtual ~Covers();
    
protected:
    virtual void run();
    
private:
    Stats *stats;
    char *dbPath;
    QByteArray extractImageID3(ID3v2::Tag *tag, Database *db);
    QByteArray extractImageOgg(Ogg::XiphComment *tag, Database *db);
    QByteArray md5(char *input, int length);
    QByteArray storeID3PictureFrame(ID3v2::AttachedPictureFrame* picture, Database* db);
};

#endif // COVERS_H
