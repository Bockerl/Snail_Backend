package com.bockerl.snailmember.infrastructure.event.consumer

import com.bockerl.snailmember.infrastructure.event.processor.FileEventProcessor

class FileEventConsumerImpl(
    private val fileEventProcessor: FileEventProcessor,
) : FileEventConsumer