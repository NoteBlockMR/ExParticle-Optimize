package net.hackermdch.exparticle.util;

import net.hackermdch.exparticle.ExParticleConfig;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class ClassExpression implements IExecutable, ICacheAble {
    /**
     * This cache used to be an unbounded HashMap.  Particle music commonly
     * bakes a different velocity/position into every cube command, which made
     * it retain every generated expression class until Minecraft was closed.
     * Access-order LRU keeps recent repeated expressions fast without letting
     * a long song turn the cache into a memory and GC leak.
     */
    private static final Map<String, ClassExpression> CACHE = new LinkedHashMap<>(256, 0.75F, true);
    private final ParticleStruct struct = new ParticleStruct();
    private final MethodHandle method;
    private boolean invalid;

    private ClassExpression(String expression) {
        var lexer = new Lexer(expression);
        var es = Parser.parseBlock(lexer);
        var codeGen = new CodeGen(es);
        try {
            var clazz = codeGen.codeGenBlock();
            method = MethodHandles.lookup().findStatic(clazz, "invoke", MethodType.methodType(int.class, ParticleStruct.class));
            for (var r : codeGen.references()) GlobalVariableUtil.handle(r, this);
            for (var r : codeGen.funcReferences()) UserFunctionUtil.handle(r, this);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    private ClassExpression(ClassExpression other) {
        method = other.method;
        invalid = other.invalid;
    }

    public static synchronized void clear() {
        CACHE.clear();
    }

    @Override
    public void invalid() {
        invalid = true;
    }

    private static ClassExpression create(String expression) {
        var instance = new ClassExpression(expression);
        CACHE.put(expression, instance);
        trimCache();
        return instance;
    }

    public static synchronized ClassExpression parse(String expression) {
        var c = CACHE.get(expression);
        if (c != null) {
            if (c.invalid) return create(expression);
            return new ClassExpression(c);
        }
        return create(expression);
    }

    private static void trimCache() {
        int limit = ExParticleConfig.config.maxExpressionCacheEntries;
        if (limit <= 0) {
            CACHE.clear();
            return;
        }
        while (CACHE.size() > limit) {
            Iterator<String> iterator = CACHE.keySet().iterator();
            iterator.next();
            iterator.remove();
        }
    }

    @Override
    public ParticleStruct getData() {
        return struct;
    }

    @Override
    public int invoke() {
        try {
            return (int) method.invokeExact(struct);
        } catch (Throwable e) {
            ClientMessageUtil.addChatMessage(e);
            throw new RuntimeException(e);
        }
    }
}
