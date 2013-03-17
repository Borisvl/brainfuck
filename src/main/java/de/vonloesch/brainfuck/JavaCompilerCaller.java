package de.vonloesch.brainfuck;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import java.net.URI;

import java.security.SecureClassLoader;

import java.util.Arrays;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

public class JavaCompilerCaller {
    public static class CharSequenceJavaFileObject extends SimpleJavaFileObject {

        /**
         * CharSequence representing the source code to be compiled.
         */
        private CharSequence content;

        /**
         * This constructor will store the source code in the internal "content" variable and register it as a source
         * code, using a URI containing the class full name.
         *
         * @param  className  name of the public class in the source code
         * @param  content    source code to compile
         */
        public CharSequenceJavaFileObject(final String className, final CharSequence content) {
            super(URI.create("string:///" + className.replace('.', '/') + Kind.SOURCE.extension), Kind.SOURCE);
            this.content = content;
        }

        /**
         * Answers the CharSequence to be compiled. It will give the source code stored in variable "content"
         */
        @Override
        public CharSequence getCharContent(final boolean ignoreEncodingErrors) {
            return content;
        }
    }

    public static class JavaClassObject extends SimpleJavaFileObject {

        /**
         * Byte code created by the compiler will be stored in this ByteArrayOutputStream so that we can later get the
         * byte array out of it and put it in the memory as an instance of our class.
         */
        protected final ByteArrayOutputStream bos = new ByteArrayOutputStream();

        /**
         * Registers the compiled class object under URI containing the class full name.
         *
         * @param  name  Full name of the compiled class
         * @param  kind  Kind of the data. It will be CLASS in our case
         */
        public JavaClassObject(final String name, final Kind kind) {
            super(URI.create("string:///" + name.replace('.', '/') + kind.extension), kind);
        }

        /**
         * Will be used by our file manager to get the byte code that can be put into memory to instantiate our class.
         *
         * @return  compiled byte code
         */
        public byte[] getBytes() {
            return bos.toByteArray();
        }

        /**
         * Will provide the compiler with an output stream that leads to our byte array. This way the compiler will
         * write everything into the byte array that we will instantiate later
         */
        @Override
        public OutputStream openOutputStream() throws IOException {
            return bos;
        }
    }

    public static class ClassFileManager extends ForwardingJavaFileManager {

        /**
         * Instance of JavaClassObject that will store the compiled bytecode of our class.
         */
        private JavaClassObject jclassObject;

        /**
         * Will initialize the manager with the specified standard java file manager.
         *
         * @param  standardManger
         */
        public ClassFileManager(final StandardJavaFileManager standardManager) {
            super(standardManager);
        }

        /**
         * Will be used by us to get the class loader for our compiled class. It creates an anonymous class extending
         * the SecureClassLoader which uses the byte code created by the compiler and stored in the JavaClassObject, and
         * returns the Class for it
         */
        @Override
        public ClassLoader getClassLoader(final Location location) {
            return new SecureClassLoader() {
                @Override
                protected Class<?> findClass(final String name) throws ClassNotFoundException {
                    byte[] b = jclassObject.getBytes();
                    return super.defineClass(name, jclassObject.getBytes(), 0, b.length);
                }
            };
        }

        /**
         * Gives the compiler an instance of the JavaClassObject so that the compiler can write the byte code into it.
         */
        @Override
        public JavaFileObject getJavaFileForOutput(final Location location, final String className, final Kind kind,
                final FileObject sibling) throws IOException {
            jclassObject = new JavaClassObject(className, kind);
            return jclassObject;
        }
    }

    public static Class compile(final String className, final String classString) {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();

        JavaFileManager fileManager = new ClassFileManager(compiler.getStandardFileManager(null, null, null));

        JavaFileObject file = new CharSequenceJavaFileObject(className, classString);

        Iterable<? extends JavaFileObject> compilationUnits = Arrays.asList(file);
        CompilationTask task = compiler.getTask(null, fileManager, diagnostics, null, null, compilationUnits);

        boolean success = task.call();
        for (Diagnostic diagnostic : diagnostics.getDiagnostics()) {
            System.out.println(diagnostic.getCode());
            System.out.println(diagnostic.getKind());
            System.out.println(diagnostic.getPosition());
            System.out.println(diagnostic.getStartPosition());
            System.out.println(diagnostic.getEndPosition());
            System.out.println(diagnostic.getSource());
            System.out.println(diagnostic.getMessage(null));
        }

        if (success) {
            Class result = null;

            try {
                result = fileManager.getClassLoader(null).loadClass(className);
            } catch (ClassNotFoundException e) { }

            return result;
        }

        return null;
    }
}
