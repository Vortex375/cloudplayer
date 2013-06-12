#include "InputReader.h"
#include <iostream>

#include <QDebug>

InputReader::InputReader(): QObject()
{
    inFile = new QFile();
    inFile->open(stdin, QIODevice::ReadOnly);
    qDebug() << "opened stdin";
}


InputReader::~InputReader()
{
    if (inFile->isOpen()) {
        inFile->close();
        qDebug() << "closed stdin";
    }
}

void InputReader::start()
{
    allowWork = true;
    
    char buf[1024];
    while (allowWork) {
        //qDebug() << "waiting for input...";
        qint64 read = inFile->readLine(buf, sizeof(buf));
        QString msg = QString::fromLocal8Bit(buf, read);
        //qDebug() << "read: " << msg;
        
        if (read < 0) {
            break;
        }
        
        msg = msg.trimmed();
        
        emit message(msg);
    }
    qDebug() << "input reader finished";
}

void InputReader::stop()
{
    allowWork = false;
    inFile->close();
    qDebug() << "closed stdin";
}


#include "InputReader.moc"
