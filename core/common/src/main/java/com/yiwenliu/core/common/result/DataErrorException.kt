package com.yiwenliu.core.common.result

class DataErrorException(val error: DataError) : Exception(error.toString())
