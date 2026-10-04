package com.sergiodev.bingo.domain.repository

/** Failure value of [BoardRepository.addBoard] when the identifier is already taken. */
class DuplicateIdentifierException(val identifier: String) :
    Exception("A board with identifier '$identifier' already exists")
