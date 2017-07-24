/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.indoqa.xml.pipeline.sax;

import java.text.ParseException;

/**
 * Parses "Text {module:{module:attribute}} more text {variable}" types of expressions. Supports escaping of braces with '\' character,
 * and nested expressions.
 */
public final class VariableExpressionTokenizer {

    /**
     * Callback for tokenizer
     */
    public interface TokenReceiver {

        public enum Type {

            OPEN, CLOSE, COLON, TEXT, MODULE, VARIABLE

        }

        /**
         * Reports parsed tokens.
         */
        void addToken(Type type, String value) throws ParseException;
    }

    private VariableExpressionTokenizer() {
    }

    /**
     * Tokenizes specified expression. Passes tokens to the reciever.
     *
     * @throws PatternSyntaxException if expression is not valid
     */
    public static void tokenize(String expression, final TokenReceiver receiver) throws ParseException {

        TokenReceiver.Type lastTokenType = null;

        int openCount = 0;
        int closeCount = 0;

        int pos = 0;
        int i;
        boolean escape = false;

        char character;
        char nextChar;
        int colonPos;
        int closePos;
        int openPos;
        for (i = 0; i < expression.length(); i++) {
            character = expression.charAt(i);

            if (escape) {
                escape = false;
            } else if (character == '\\' && i < expression.length()) {
                nextChar = expression.charAt(i + 1);
                if (nextChar == '{' || nextChar == '}') {
                    expression = expression.substring(0, i) + expression.substring(i + 1);
                    escape = true;
                    i--;
                }
            } else if (character == '{') {
                if (i > pos) {
                    lastTokenType = TokenReceiver.Type.TEXT;
                    receiver.addToken(lastTokenType, expression.substring(pos, i));
                }

                openCount++;
                lastTokenType = TokenReceiver.Type.OPEN;
                receiver.addToken(lastTokenType, null);

                colonPos = indexOf(expression, ':', i);
                closePos = indexOf(expression, '}', i);
                openPos = indexOf(expression, '{', i);

                if (openPos < colonPos && openPos < closePos) {
                    throw new ParseException(expression, i);
                }

                if (colonPos < closePos) {
                    // we've found a module
                    lastTokenType = TokenReceiver.Type.MODULE;
                    receiver.addToken(lastTokenType, expression.substring(i + 1, colonPos));
                    i = colonPos - 1;
                } else {
                    // Unprefixed name: variable
                    lastTokenType = TokenReceiver.Type.VARIABLE;
                    receiver.addToken(lastTokenType, expression.substring(i + 1, closePos));
                    i = closePos - 1;
                }

                pos = i + 1;
            } else if (character == '}') {
                if (i > 0 && expression.charAt(i - 1) == '\\') {
                    continue;
                }
                if (i > pos) {
                    lastTokenType = TokenReceiver.Type.TEXT;
                    receiver.addToken(lastTokenType, expression.substring(pos, i));
                }

                closeCount++;
                lastTokenType = TokenReceiver.Type.CLOSE;
                receiver.addToken(lastTokenType, null);

                pos = i + 1;
            } else if (character == ':') {
                if (lastTokenType != TokenReceiver.Type.MODULE || i != pos) {
                    // this colon isn't part of a module reference
                    continue;
                }

                lastTokenType = TokenReceiver.Type.COLON;
                receiver.addToken(lastTokenType, null);
                pos = i + 1;
            }
        }

        if (i > pos) {
            lastTokenType = TokenReceiver.Type.TEXT;
            receiver.addToken(lastTokenType, expression.substring(pos, i));
        }

        if (openCount != closeCount) {
            throw new ParseException(expression, expression.length());
        }
    }

    private static int indexOf(final String expression, final char chr, final int pos) {

        final int location = expression.indexOf(chr, pos + 1);
        return location == -1 ? expression.length() : location;
    }
}
