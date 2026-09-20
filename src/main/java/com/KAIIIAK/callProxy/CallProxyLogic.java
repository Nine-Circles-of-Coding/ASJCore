package com.KAIIIAK.callProxy;

import com.KAIIIAK.KASMLib.asm.commons.KASMSimpleRemapper;
import com.KAIIIAK.classManipulators.SomeUtil;
import com.KAIIIAK.nullsafety.Opt;
import gloomyfolken.hooklib.asm.HookLogger;
import net.minecraft.launchwrapper.IClassTransformer;
import org.apache.commons.io.IOUtils;
import org.objectweb.asm.*;
import org.objectweb.asm.commons.RemappingClassAdapter;
import org.objectweb.asm.tree.*;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class CallProxyLogic implements IClassTransformer {

    public static HookLogger logger = new HookLogger.Log4JLogger("CallProxy");
    public static Map<String, String> remapperMap;
    public static KASMSimpleRemapper remapper;

    static {
        remapperMap = new HashMap<>();
        remapper = new KASMSimpleRemapper(remapperMap);
    }

    public static void register(String clazz) {
        logger.debug("Parsing CallProxy container " + clazz);
        Opt.it(CallProxyLogic.class.getResourceAsStream('/' + clazz.replace('.', '/') + ".class"), it -> {
            try {
                processContainerBytes(IOUtils.toByteArray(it), clazz);
            } catch (IOException e) {
                logger.error(String.format("Can not parse CallProxy container %s", clazz), e);
                throw new RuntimeException(e);
            }
        });
    }

    public static void processContainerBytes(byte[] clazzBytes, String clazz) {
        try {
            ClassReader classReader = new ClassReader(clazzBytes);
            ClassNode classNode = new ClassNode();
            classReader.accept(classNode, 0);
            AnnotationNode callProxyAnnotation = null;
            for (AnnotationNode annotationNode : Opt.it(classNode.visibleAnnotations)) {
                if (annotationNode.desc.equals("Lcom/KAIIIAK/callProxy/CallProxy;")) {
                    callProxyAnnotation = annotationNode;
                    break;
                }
            }
            for (AnnotationNode annotationNode : Opt.it(classNode.invisibleAnnotations)) {
                if (annotationNode.desc.equals("Lcom/KAIIIAK/callProxy/CallProxy;")) {
                    callProxyAnnotation = annotationNode;
                    break;
                }
            }
            if (callProxyAnnotation == null) {
                logger.error("Can not find @CallProxy at " + clazz);
                return;
            }
            logger.debug("@CallProxy found at " + clazz);
            Map<String, Object> annotationArgs = SomeUtil.convertListToMap(callProxyAnnotation.values);
            String target = (String) annotationArgs.get("target");
            if (target == null || target.isEmpty()) {
                logger.error("Can not find \"target\" at @CallProxy at " + clazz);
                return;
            }

            remapperMap.put(clazz.replace('.', '/'), target.replace('.', '/'));
            logger.debug("Registered @CallProxy '" + clazz + "' to be remapped to '" + target + "'");
        } catch (Exception e) {
            logger.error("Can not parse CallProxy container", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (basicClass == null)
            return null;

        if (remapperMap.containsKey(transformedName.replace('.', '/')) || remapperMap.containsKey(name.replace('.', '/')))
            return basicClass;

        AtomicInteger changes = new AtomicInteger();
        remapper.changes = changes;
        
        try {
            ClassReader cr = new ClassReader(basicClass);
            ClassWriter cw = new ClassWriter(cr, 0);
            ClassVisitor cv = new RemappingClassAdapter(cw, remapper);

            cv = new ClassVisitor(Opcodes.ASM5, cv) {
                public MethodVisitor visitMethod(int access, String mName, String mDesc, String signature, String[] exceptions) {
                    return new MethodVisitor(Opcodes.ASM5, super.visitMethod(access, mName, mDesc, signature, exceptions)) {
                        public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
                            if (opcode == Opcodes.INVOKESTATIC && "c".equals(name) && remapperMap.containsKey(owner)) {
                                String target = remapperMap.get(owner);

                                Type methodType = Type.getMethodType(desc);
                                Type[] args = methodType.getArgumentTypes();
                                Type ret = methodType.getReturnType();
                                if (args.length == 1 && args[0].getInternalName().equals(target) && ret.getInternalName().equals(owner)) {
                                    logger.trace("Removed CallProxy cast at " + transformedName + "." + mName + mDesc);
                                    changes.incrementAndGet();
                                    return;
                                }
                            }
                            super.visitMethodInsn(opcode, owner, name, desc, itf);
                        }
                    };
                }
            };

            cr.accept(cv, ClassReader.EXPAND_FRAMES);
            
            if (changes.get() > 0) {
                logger.debug("Remapped " + changes.get() + " calls in " + transformedName);
                return cw.toByteArray();
            }
        } catch (Throwable e) {
            logger.error("Exception remapping class " + transformedName, e);
        } finally {
            remapper.changes = null;
        }
        return basicClass;
    }
}