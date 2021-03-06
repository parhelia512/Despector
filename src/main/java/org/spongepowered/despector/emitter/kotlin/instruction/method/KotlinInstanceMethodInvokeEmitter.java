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
package org.spongepowered.despector.emitter.kotlin.instruction.method;

import org.spongepowered.despector.ast.generic.ClassTypeSignature;
import org.spongepowered.despector.ast.generic.TypeSignature;
import org.spongepowered.despector.ast.insn.Instruction;
import org.spongepowered.despector.ast.insn.misc.NewArray;
import org.spongepowered.despector.ast.insn.var.LocalAccess;
import org.spongepowered.despector.ast.stmt.invoke.InstanceMethodInvoke;
import org.spongepowered.despector.emitter.java.JavaEmitterContext;
import org.spongepowered.despector.emitter.java.instruction.InstanceMethodInvokeEmitter;
import org.spongepowered.despector.util.TypeHelper;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * An emitter for kotlin instance method invoke instructions.
 */
public class KotlinInstanceMethodInvokeEmitter extends InstanceMethodInvokeEmitter {

    private static final Set<String> NO_CALLEE = new HashSet<>();
    private static final Set<String> NO_PARAMS = new HashSet<>();
    private static final Map<String, SpecialMethodEmitter<InstanceMethodInvoke>> SPECIAL = new HashMap<>();

    static {
        NO_CALLEE.add("Ljava/io/PrintStream;println");
        NO_CALLEE.add("Ljava/io/PrintStream;print");

        NO_PARAMS.add("Ljava/lang/String;length");

        SPECIAL.put("Ljava/lang/StringBuilder;toString", new StringConcatEmitter());
        SPECIAL.put("Ljava/util/List;contains", new ListContainsEmitter());
        MapGetEmitter map_get = new MapGetEmitter();
        SPECIAL.put("Ljava/util/Map;get", map_get);
        SPECIAL.put("Ljava/util/HashMap;get", map_get);
        SPECIAL.put("Ljava/lang/String;charAt", map_get);
        // TODO operator overloading, any get method can be simplified in this way
        MapPutEmitter map_put = new MapPutEmitter();
        SPECIAL.put("Ljava/util/Map;put", map_put);
        SPECIAL.put("Ljava/util/HashMap;put", map_put);
    }

    @Override
    public void emit(JavaEmitterContext ctx, InstanceMethodInvoke arg, TypeSignature type) {
        String key = arg.getOwner() + arg.getMethodName();
        SpecialMethodEmitter<InstanceMethodInvoke> special = SPECIAL.get(key);
        if (special != null && special.emit(ctx, arg, type)) {
            return;
        }
        if (arg.getMethodName().equals("<init>")) {
            if (ctx.getType() != null) {
                if (arg.getOwnerName().equals(ctx.getType().getName())) {
                    ctx.printString("this");
                } else {
                    ctx.printString("super");
                }
            } else {
                ctx.printString("super");
            }
        } else {
            if (!NO_CALLEE.contains(key)) {
                if (arg.getCallee() instanceof LocalAccess && ctx.getMethod() != null && !ctx.getMethod().isStatic()) {
                    LocalAccess local = (LocalAccess) arg.getCallee();
                    if (local.getLocal().getIndex() == 0) {
                        if (ctx.getType() != null && !arg.getOwnerName().equals(ctx.getType().getName())) {
                            ctx.printString("super.");
                        }
                    } else {
                        ctx.emit(local, null);
                        ctx.printString(".");
                    }
                } else {
                    ctx.emit(arg.getCallee(), ClassTypeSignature.of(arg.getOwner()));
                    ctx.printString(".");
                }
            }
            ctx.printString(arg.getMethodName());
        }
        if (NO_PARAMS.contains(key)) {
            return;
        }
        ctx.printString("(");
        List<String> param_types = TypeHelper.splitSig(arg.getMethodDescription());
        for (int i = 0; i < arg.getParameters().length; i++) {
            Instruction param = arg.getParameters()[i];
            if (i == arg.getParameters().length - 1 && param instanceof NewArray) {
                NewArray varargs = (NewArray) param;
                for (int o = 0; o < varargs.getInitializer().length; o++) {
                    ctx.emit(varargs.getInitializer()[o], varargs.getType());
                    if (o < varargs.getInitializer().length - 1) {
                        ctx.printString(", ");
                        ctx.markWrapPoint();
                    }
                }
                break;
            }
            ctx.emit(param, ClassTypeSignature.of(param_types.get(i)));
            if (i < arg.getParameters().length - 1) {
                ctx.printString(", ");
                ctx.markWrapPoint();
            }
        }
        ctx.printString(")");
    }

}
