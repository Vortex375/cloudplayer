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


#ifndef APPLICATION_H
#define APPLICATION_H

#include <QCoreApplication>
#include <QThread>

#include "InitException.h"
#include "MediaConvert.h"
#include "InputReader.h"

class Application : public QCoreApplication {
Q_OBJECT

public:
    Application(int& argc, char** argv, int  = ApplicationFlags);
    virtual ~Application();

//protected:
    
public slots:
  void onError(char* msg);
  
  void testMessage(QString msg);
  
private:
    MediaConvert *mediaConvert;
    InputReader *inputReader;
    QThread *inputThread;
};

#endif // APPLICATION_H
