package org.unmojang.ProxyFix;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.lang.Class;
import java.security.ProtectionDomain;
import java.io.*;
import org.objectweb.asm.*;

public class Premain {
    public static void premain(String agentArgs, Instrumentation inst) {
        System.out.println("[ProxyFix] Running...");
        inst.addTransformer(new URLTransformer(), true);
        try {
            Class<?> urlClass = Class.forName("java.net.URL");
            inst.retransformClasses(urlClass);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static class URLTransformer implements ClassFileTransformer {
        @Override
        public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
                                ProtectionDomain protectionDomain, byte[] classFileBuffer) {
            if ("java/net/URL".equals(className)) {
                return transformURLClass(classFileBuffer);
            }
            return classFileBuffer;
        }

        private byte[] transformURLClass(byte[] classFileBuffer) {
            ClassReader classReader = new ClassReader(classFileBuffer);
            ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);

            classReader.accept(new ClassVisitor(Opcodes.ASM9, classWriter) {
                @Override
                public MethodVisitor visitMethod(int access, String name, String descriptor,
                                                 String signature, String[] exceptions) {
                    MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);

                    if ("openConnection".equals(name) &&
                        "(Ljava/net/Proxy;)Ljava/net/URLConnection;".equals(descriptor)) {
                        System.out.println("[ProxyFix] Rewriting URL.openConnection");
                        return new MethodVisitor(Opcodes.ASM9, mv) {
                            @Override
                            public void visitCode() {
                                super.visitCode();
                                mv.visitVarInsn(Opcodes.ALOAD, 0);
                                mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                                        "java/net/URL",
                                        "openConnection",
                                        "()Ljava/net/URLConnection;",
                                        false);
                                mv.visitInsn(Opcodes.ARETURN);
                            }
                        };
                    }
                    return mv;
                }
            }, 0);
            System.out.println("[ProxyFix] Transformed URL class.");

            return classWriter.toByteArray();
        }
    }
}
