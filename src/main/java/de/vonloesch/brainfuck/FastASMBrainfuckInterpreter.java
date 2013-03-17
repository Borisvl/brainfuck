package de.vonloesch.brainfuck;

import java.lang.reflect.InvocationTargetException;

import java.util.Map;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Super fast bf interpreter. Uses ASM to compile bf code to Java bytecode. Instead of ASMBrainfuckInterpreter it
 * ignores the states and outputs directly to sysout.
 *
 * @author  Boris von Loesch
 */
public class FastASMBrainfuckInterpreter implements BrainfuckInterpreter, Opcodes {
    private static final String CLASS_NAME = "BF";

    public static class DynamicClassLoader extends ClassLoader {
        public Class<?> define(final String className, final byte[] bytecode) {
            return super.defineClass(className, bytecode, 0, bytecode.length);
        }
    }

    /**
     * @return  null
     */
    @Override
    public State run(final String program, final State s) {
        Map<String, Integer> methodMap = Utils.getMethods(program);
        ClassWriter cw = new ClassWriter(0);
        FieldVisitor fv;
        MethodVisitor mv;

        cw.visit(V1_6, ACC_PUBLIC + ACC_SUPER, CLASS_NAME, null, "java/lang/Object", null);
        cw.visitSource(CLASS_NAME + ".java", null);
        fv = cw.visitField(ACC_PRIVATE + ACC_STATIC, "cell", "[B", null, null);
        fv.visitEnd();
        fv = cw.visitField(ACC_PRIVATE + ACC_STATIC, "p", "I", null, null);
        fv.visitEnd();
        fv = cw.visitField(ACC_PRIVATE + ACC_STATIC, "add", "B", null, null);
        fv.visitEnd();
        mv = cw.visitMethod(ACC_STATIC, "<clinit>", "()V", null, null);
        mv.visitCode();

        mv.visitLdcInsn(new Integer(65536));
        mv.visitIntInsn(NEWARRAY, T_BYTE);
        mv.visitFieldInsn(PUTSTATIC, CLASS_NAME, "cell", "[B");

        mv.visitInsn(ICONST_0);
        mv.visitFieldInsn(PUTSTATIC, CLASS_NAME, "p", "I");

        mv.visitInsn(ICONST_0);
        mv.visitFieldInsn(PUTSTATIC, CLASS_NAME, "add", "B");
        mv.visitInsn(RETURN);
        mv.visitMaxs(1, 0);
        mv.visitEnd();

        {
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
        }

        mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "run", "()V", null, null);
        mv.visitCode();
        mv.visitFieldInsn(GETSTATIC, CLASS_NAME, "p", "I");
        mv.visitVarInsn(ISTORE, 0);

        convertMethod2BC(mv, methodMap, program, false);
        mv.visitVarInsn(ILOAD, 0);
        mv.visitFieldInsn(PUTSTATIC, CLASS_NAME, "p", "I");
        mv.visitInsn(RETURN);

        mv.visitMaxs(4, 1);
        mv.visitEnd();
        for (Map.Entry<String, Integer> entry : methodMap.entrySet()) {
            mv = cw.visitMethod(ACC_PRIVATE + ACC_STATIC, "meth" + entry.getValue(), "(I)I", null, null);
            mv.visitCode();

            convertMethod2BC(mv, methodMap, entry.getKey(), true);
            mv.visitVarInsn(ILOAD, 0);
            mv.visitInsn(IRETURN);

            mv.visitMaxs(4, 1);
            mv.visitEnd();
        }

        DynamicClassLoader loader = new DynamicClassLoader();
        Class<?> helloWorldClass = loader.define(CLASS_NAME, cw.toByteArray());
        try {
            helloWorldClass.getMethod("run").invoke(null);
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
            final String methodBody, final boolean isLoop) {
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
                mv.visitIincInsn(0, -i);
                break;

            case '>' :
                i = Utils.getRepeatCount(p, chars);
                mv.visitIincInsn(0, i);
                break;

            case '+' :
                i = Utils.getRepeatCount(p, chars);
                mv.visitFieldInsn(GETSTATIC, CLASS_NAME, "cell", "[B");
                mv.visitVarInsn(ILOAD, 0);
                mv.visitInsn(DUP2);
                mv.visitInsn(BALOAD);
                mv.visitIntInsn(BIPUSH, i);
                mv.visitInsn(IADD);
                mv.visitInsn(I2B);
                mv.visitInsn(BASTORE);
                break;

            case '-' :
                i = Utils.getRepeatCount(p, chars);
                mv.visitFieldInsn(GETSTATIC, CLASS_NAME, "cell", "[B");
                mv.visitVarInsn(ILOAD, 0);
                mv.visitInsn(DUP2);
                mv.visitInsn(BALOAD);
                mv.visitIntInsn(BIPUSH, i);
                mv.visitInsn(ISUB);
                mv.visitInsn(I2B);
                mv.visitInsn(BASTORE);
                break;

            case '.' :
                mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
                mv.visitFieldInsn(GETSTATIC, CLASS_NAME, "cell", "[B");
                mv.visitVarInsn(ILOAD, 0);
                mv.visitInsn(BALOAD);
                mv.visitInsn(I2C);
                mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "print", "(C)V");
                break;

            case '[' :

                int end = Utils.findEndBracket(p, methodBody);
                String body = methodBody.substring(p + 1, end);

                // Optimize
                if ("-".equals(body) || "+".equals(body)) {
                    mv.visitFieldInsn(GETSTATIC, CLASS_NAME, "cell", "[B");
                    mv.visitVarInsn(ILOAD, 0);
                    mv.visitInsn(ICONST_0);
                    mv.visitInsn(BASTORE);
                } else {
                    mv.visitVarInsn(ILOAD, 0);
                    mv.visitMethodInsn(INVOKESTATIC, CLASS_NAME, "meth" + methodMap.get(body), "(I)I");
                    mv.visitVarInsn(ISTORE, 0);
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
            mv.visitFieldInsn(GETSTATIC, CLASS_NAME, "cell", "[B");
            mv.visitVarInsn(ILOAD, 0);
            mv.visitInsn(BALOAD);
            mv.visitJumpInsn(IFNE, l2);
        }

        return mv;
    }

}
