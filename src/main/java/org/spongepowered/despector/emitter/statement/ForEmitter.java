/*
 * The MIT License (MIT)
 *
 * Copyright (c) Despector <https://despector.voxelgenesis.com>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.spongepowered.despector.emitter.statement;

import org.spongepowered.despector.ast.members.insn.branch.For;
import org.spongepowered.despector.emitter.EmitterContext;
import org.spongepowered.despector.emitter.StatementEmitter;

/**
 * An emitter for for loops.
 */
public class ForEmitter implements StatementEmitter<For> {

    @Override
    public void emit(EmitterContext ctx, For loop, boolean semicolon) {
        ctx.printString("for");
        ctx.printString(" ", ctx.getFormat().insert_space_before_opening_paren_in_for);
        ctx.printString("(");
        ctx.printString(" ", ctx.getFormat().insert_space_after_opening_paren_in_for);
        if (loop.getInit() != null) {
            ctx.emit(loop.getInit(), false);
        }
        ctx.printString("; ");
        ctx.emit(loop.getCondition());
        ctx.printString("; ");
        if (loop.getIncr() != null) {
            ctx.emit(loop.getIncr(), false);
        }
        ctx.printString(") {");
        ctx.newLine();
        if (!loop.getBody().getStatements().isEmpty()) {
            ctx.indent();
            ctx.emitBody(loop.getBody());
            ctx.dedent();
            ctx.newLine();
        }
        ctx.printIndentation();
        ctx.printString("}");
    }

}
