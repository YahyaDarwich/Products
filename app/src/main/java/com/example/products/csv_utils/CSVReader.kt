package com.example.products.csv_utils

import java.io.BufferedReader
import java.io.IOException
import java.io.Reader

/**
 * Copyright 2005 Bytecode Pty Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
 * The code copied from http://opencsv.sourceforge.net/
 *
 * While incorporating into secrets, the following changes were made:
 *
 * - Added support of generics
 * - removed the following methods to keep the bytecode smaller:
 *   readAll(), some constructors
 */

/**
 * A very simple CSV reader released under a commercial-friendly license.
 *
 * @author Glen Smith
 */
class CSVReader @JvmOverloads constructor(
    reader: Reader?,
    private val separator: Char = DEFAULT_SEPARATOR,
    private val quotechar: Char = DEFAULT_QUOTE_CHARACTER,
    private val skipLines: Int = DEFAULT_SKIP_LINES
) {
    private val br = BufferedReader(reader)

    private var hasNext = true

    private var linesSkiped = false

    /**
     * Constructs CSVReader with supplied separator and quote char.
     *
     * @param reader
     * the reader to an underlying CSV source.
     * @param separator
     * the delimiter to use for separating entries
     * @param quotechar
     * the character to use for quoted elements
     * @param skipLines
     * the line number to skip for start reading
     */

    /**
     * Reads the next line from the buffer and converts to a string array.
     *
     * @return a string array with each comma-separated element as a separate
     * entry.
     *
     * @throws IOException
     * if bad things happen during the read
     */
    @Throws(IOException::class)
    fun readNext(): Array<String>? {
        val nextLine = nextLine
        return if (hasNext) parseLine(nextLine) else null
    }

    @get:Throws(IOException::class)
    private val nextLine: String?
        /**
         * Reads the next line from the file.
         *
         * @return the next line from the file without trailing newline
         * @throws IOException
         * if bad things happen during the read
         */
        get() {
            if (!this.linesSkiped) {
                for (i in 0 until skipLines) {
                    br.readLine()
                }
                this.linesSkiped = true
            }
            val nextLine = br.readLine()
            if (nextLine == null) {
                hasNext = false
            }
            return if (hasNext) nextLine else null
        }

    /**
     * Parses an incoming String and returns an array of elements.
     *
     * @param nextLine
     * the string to parse
     * @return the comma-tokenized list of elements, or null if nextLine is null
     * @throws IOException if bad things happen during the read
     */
    @Throws(IOException::class)
    private fun parseLine(nextLine: String?): Array<String>? {
        var nextLine: String? = nextLine ?: return null

        val tokensOnThisLine: MutableList<String> = ArrayList()
        var sb = StringBuffer()
        var inQuotes = false
        do {
            if (inQuotes) {
                // continuing a quoted section, reappend newline
                sb.append("\n")
                nextLine = this.nextLine
                if (nextLine == null) break
            }
            var i = 0
            while (i < nextLine!!.length) {
                val c = nextLine[i]
                if (c == quotechar) {
                    // this gets complex... the quote may end a quoted block, or escape another quote.
                    // do a 1-char lookahead:
                    if ( // there is indeed another character to check.
                        inQuotes && nextLine.length > i + 1 && nextLine[i + 1] == quotechar) { // ..and that char. is a quote also.
                        // we have two quote chars in a row == one quote char, so consume them both and
                        // put one on the token. we do *not* exit the quoted text.
                        sb.append(nextLine[i + 1])
                        i++
                    } else {
                        inQuotes = !inQuotes
                        // the tricky case of an embedded quote in the middle: a,bc"d"ef,g
                        if ( //not at the begining of an escape sequence
                            (i > 2 && nextLine[i - 1] != separator) && nextLine.length > (i + 1) && nextLine[i + 1] != this.separator //not at the	end of an escape sequence
                        ) {
                            sb.append(c)
                        }
                    }
                } else if (c == separator && !inQuotes) {
                    tokensOnThisLine.add(sb.toString())
                    sb = StringBuffer() // start work on next token
                } else {
                    sb.append(c)
                }
                i++
            }
        } while (inQuotes)
        tokensOnThisLine.add(sb.toString())
        return tokensOnThisLine.toTypedArray<String>()
    }

    /**
     * Closes the underlying reader.
     *
     * @throws IOException if the close fails
     */
    @Throws(IOException::class)
    fun close() {
        br.close()
    }

    companion object {
        /** The default separator to use if none is supplied to the constructor.  */
        const val DEFAULT_SEPARATOR: Char = ','

        /**
         * The default quote character to use if none is supplied to the
         * constructor.
         */
        const val DEFAULT_QUOTE_CHARACTER: Char = '"'

        /**
         * The default line to start reading.
         */
        const val DEFAULT_SKIP_LINES: Int = 0
    }
}
