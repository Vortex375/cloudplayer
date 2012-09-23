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


#ifndef UPDATER_H
#define UPDATER_H
#include <QtCore/QThread>

#include "Database.h"
#include "BlockingQueue.h"
#include "Stats.h"

class Updater : public QThread
{
Q_OBJECT
public:
    Updater(BlockingQueue *q, Database *db, Stats *s);
    virtual ~Updater();
protected:
    virtual void run();
private:
    BlockingQueue *queue;
    Stats *stats;
    Database *db;
};

#endif // UPDATER_H
