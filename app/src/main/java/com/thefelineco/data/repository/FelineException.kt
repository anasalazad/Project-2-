package com.thefelineco.data.repository

/** A business-rule failure with a message that is safe to show to the user. */
class FelineException(message: String) : Exception(message)
