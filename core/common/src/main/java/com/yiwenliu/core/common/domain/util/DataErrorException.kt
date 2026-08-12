package com.yiwenliu.core.common.domain.util

class DataErrorException(val error: DataError) : Exception(error.toString())
