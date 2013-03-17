package de.vonloesch.brainfuck;

import java.lang.reflect.InvocationTargetException;

import java.util.Map;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 * Brainfuck implementation that first compiles brainfuck to java bytecode with ASM and starts the code afterwards. Very
 * fast!
 *
 * @author  Boris von Loesch
 */
public class ASMBrainfuckInterpreter implements BrainfuckInterpreter, Opcodes {

    private static final String CLASS_NAME = "BF";

    public static class DynamicClassLoader extends ClassLoader {
        public Class<?> define(final String className, final byte[] bytecode) {
            return super.defineClass(className, bytecode, 0, bytecode.length);
        }
    }

    @Override
    public State run(final String program, final State s) {
        Map<String, Integer> methodMap = Utils.getMethods(program);
        ClassWriter cw = new ClassWriter(0);
        MethodVisitor mv;
        final String stateClass = Type.getInternalName(s.getClass());

        cw.visit(V1_6, ACC_PUBLIC + ACC_SUPER, CLASS_NAME, null, "java/lang/Object", null);
        cw.visitSource(CLASS_NAME + ".java", null);
        mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
        mv.visitCode();

        Label l0 = new Label();
        mv.visitLabel(l0);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V");
        mv.visitInsn(RETURN);

        Label l1 = new Label();
        mv.visitLabel(l1);
        mv.visitLocalVariable("this", "LHelloWorld;", null, l0, l1, 0);
        mv.visitMaxs(1, 1);
        mv.visitEnd();

        mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "run", "(L" + stateClass + ";)V", null, null);
        mv.visitCode();

        convertMethod2BC(mv, methodMap, program, false, stateClass, s instanceof StatusState);
        mv.visitInsn(RETURN);

        mv.visitMaxs(2, 1);
        mv.visitEnd();
        for (Map.Entry<String, Integer> entry : methodMap.entrySet()) {
            mv = cw.visitMethod(ACC_PRIVATE + ACC_STATIC, "meth" + entry.getValue(), "(L" + stateClass + ";)V", null,
                    null);
            mv.visitCode();

            convertMethod2BC(mv, methodMap, entry.getKey(), true, stateClass, s instanceof StatusState);
            mv.visitInsn(RETURN);

            mv.visitMaxs(2, 1);
            mv.visitEnd();
        }

        DynamicClassLoader loader = new DynamicClassLoader();
        Class<?> helloWorldClass = loader.define(CLASS_NAME, cw.toByteArray());
        try {
            helloWorldClass.getMethod("run", s.getClass()).invoke(null, s);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        }

        return null;
    }

    public MethodVisitor convertMethod2BC(final MethodVisitor mv, final Map<String, Integer> methodMap,
            final String methodBody, final boolean isLoop, final String stateClass, final boolean hasStatus) {
        Label l1 = new Label();
        Label l2 = new Label();
        if (isLoop) {
            mv.visitJumpInsn(GOTO, l1);
            mv.visitLabel(l2);
            mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
        }

        char[] chars = methodBody.toCharArray();
        int p = 0;
        while (p < chars.length) {
            char c = chars[p];
            int i = 1;
            switch (c) {

            case '<' :
                i = Utils.getRepeatCount(p, chars);
                mv.visitVarInsn(ALOAD, 0);
                mv.visitIntInsn(BIPUSH, -i);
                mv.visitMethodInsn(INVOKEVIRTUAL, stateClass, "addToPointer", "(I)V");
                break;

            case '>' :
                i = Utils.getRepeatCount(p, chars);
                mv.visitVarInsn(ALOAD, 0);
                mv.visitIntInsn(BIPUSH, i);
                mv.visitMethodInsn(INVOKEVIRTUAL, stateClass, "addToPointer", "(I)V");
                break;

            case '+' :
                i = Utils.getRepeatCount(p, chars);
                mv.visitVarInsn(ALOAD, 0);
                mv.visitIntInsn(BIPUSH, i);
                mv.visitMethodInsn(INVOKEVIRTUAL, stateClass, "addToValue", "(I)V");
                break;

            case '-' :
                i = Utils.getRepeatCount(p, chars);
                mv.visitVarInsn(ALOAD, 0);
                mv.visitIntInsn(BIPUSH, -i);
                mv.visitMethodInsn(INVOKEVIRTUAL, stateClass, "addToValue", "(I)V");
                break;

            case '.' :
                mv.visitVarInsn(ALOAD, 0);
                mv.visitMethodInsn(INVOKEVIRTUAL, stateClass, "print", "()V");
                break;

            case '[' :

                int end = Utils.findEndBracket(p, methodBody);
                String body = methodBody.substring(p + 1, end);

                // Optimize
                if ("-".equals(body) || "+".equals(body)) {
                    mv.visitVarInsn(ALOAD, 0);
                    mv.visitMethodInsn(INVOKEVIRTUAL, stateClass, "clearValue", "()V");
                } else {
                    mv.visitVarInsn(ALOAD, 0);
                    mv.visitMethodInsn(INVOKESTATIC, CLASS_NAME, "meth" + methodMap.get(body),
                            "(L" + stateClass + ";)V");
                }

                p = end;

                break;

            default :
                break;
            }

            p += i;
        }

        if (isLoop) {
            mv.visitLabel(l1);
            mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKEVIRTUAL, stateClass, "getValue", "()B");
            if (hasStatus) {
                Label l12 = new Label();
                mv.visitJumpInsn(IFEQ, l12);
                mv.visitVarInsn(ALOAD, 0);
                mv.visitMethodInsn(INVOKEVIRTUAL, stateClass, "isTerminated", "()Z");
                mv.visitJumpInsn(IFEQ, l2);
                mv.visitLabel(l12);
                mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
            } else {
                mv.visitJumpInsn(IFNE, l2);
            }
        }

        return mv;
    }
}
