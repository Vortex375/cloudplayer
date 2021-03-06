#include "Stats.h"

Stats::Stats() {
    processed = 0;
    found = 0;
}

Stats::~Stats() {}

long int Stats::getFound() {
    QMutexLocker locker(&mutex);
    return found;
}

long int Stats::getProcessed() {
    QMutexLocker locker(&mutex);
    return processed;
}

void Stats::incrementFound() {
    QMutexLocker locker(&mutex);
    found++;
}

void Stats::incrementProcessed() {
    QMutexLocker locker(&mutex);
    processed++;
}

void Stats::setFound(long found)
{
    QMutexLocker locker(&mutex);
    this->found = found;
}

void Stats::setProcessed(long processed)
{
    QMutexLocker locker(&mutex);
    this->processed = processed;
}

#include "Stats.moc"