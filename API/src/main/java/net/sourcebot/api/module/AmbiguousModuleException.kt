package net.sourcebot.api.module

import java.io.File

class AmbiguousModuleException(
    name: String,
    firstIndexed: File,
    lastIndexed: File
) : RuntimeException("Module '$name' from ${firstIndexed.path} is duplicated by ${lastIndexed.path}!")