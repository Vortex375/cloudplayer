#include "InputReader.h"

InputReader::InputReader(QFile *inFile): QObject()
{
    this->inFile = inFile;
}


InputReader::~InputReader()
{
    inFile->close();
}

void InputReader::start()
{
    allowWork = true;
    
    char buf[1024];
    while (allowWork) {
        qint64 read = inFile->readLine(buf, sizeof(buf));
        
        if (read < 0) {
            break;
        }
        
        QString msg = QString::fromLocal8Bit(buf, read);
        msg = msg.trimmed();
        
        emit message(msg);
    }
}

void InputReader::stop()
{
    allowWork = false;
}


#include "InputReader.moc"
