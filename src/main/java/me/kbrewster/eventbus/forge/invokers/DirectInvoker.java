package me.kbrewster.eventbus.forge.invokers;

import me.kbrewster.eventbus.forge.invokers.direct.DirectInvokerClassLoader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

import static org.objectweb.asm.Opcodes.*;

/**
 * Adapted from LWJEB
 * Changed: adapted to KEventBus structure
 */
public class DirectInvoker implements InvokerType {

    private final DirectInvokerClassLoader classLoader;

    public DirectInvoker() {
        this(DirectInvoker.class.getClassLoader());
    }

    public DirectInvoker(ClassLoader parent) {
         this.classLoader = new DirectInvokerClassLoader(parent);
    }

    @Override
    public Object setup(Object object, Class<?> clazz, Class<?> parameterClazz, Method method) throws Throwable {
        Class<?> parent = object.getClass();
        String name = "keventbus/generated/" + clazz.getName().replace('.', '/') + "/" + getUniqueMethodName(method);
        String nameAlt = name.replace('/', '.');
        Class<?> existing = classLoader.getClassIfPresent(name);
        if (existing != null) {
            return existing.getConstructor().newInstance();
        }

        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);

        cw.visit(V1_8, ACC_PUBLIC, name, null, "java/lang/Object", new String[]{InvokerType.SubscriberMethodParent.class.getName().replace('.', '/')});

        MethodVisitor con = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
        con.visitCode();
        con.visitVarInsn(ALOAD, 0);
        con.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
        con.visitInsn(RETURN);
        con.visitMaxs(1, 1);
        con.visitEnd();

        MethodVisitor invoke = cw.visitMethod(ACC_PUBLIC, "invoke", "(Ljava/lang/Object;Ljava/lang/Object;)V", null, null); // event, parent
        invoke.visitCode();
        invoke.visitVarInsn(ALOAD, 1);
        invoke.visitTypeInsn(CHECKCAST, parameterClazz.getName().replace('.', '/'));
        invoke.visitVarInsn(ASTORE, 3);
        invoke.visitVarInsn(ALOAD, 2);
        invoke.visitTypeInsn(CHECKCAST, parent.getName().replace('.', '/'));
        invoke.visitVarInsn(ASTORE, 4);
        invoke.visitVarInsn(ALOAD, 4);
        invoke.visitVarInsn(ALOAD, 3);
        invoke.visitMethodInsn(INVOKEVIRTUAL, parent.getName().replace('.', '/'), method.getName(), getMethodDescriptor(method), false);
        invoke.visitInsn(RETURN);
        invoke.visitMaxs(2, 4);
        invoke.visitEnd();

        cw.visitEnd();

        byte[] bytes = cw.toByteArray();

        Class<?> compiledClass = classLoader.createClass(nameAlt, bytes);

        return compiledClass.getConstructor().newInstance();
    }

    /**
     * Gets a unique method name from a method instance.
     *
     * @param method The method.
     * @return The unique name.
     */
    private static String getUniqueMethodName(Method method) {
        StringBuilder parameters = new StringBuilder();
        for (Parameter parameter : method.getParameters()) {
            parameters.append(parameter.getType().getName().replace('.', '_').replace(" ", "_"));
        }
        return method.getName().replace(" ", "_") + parameters;
    }

    /**
     * Gets a method descriptor from a method object.
     *
     * @param method The method.
     * @return The descriptor.
     */
    private static String getMethodDescriptor(Method method) {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("(");

        for (Class<?> parameterType : method.getParameterTypes()) {
            Class<?> current = parameterType;
            while (current.isArray()) {
                stringBuilder.append('[');
                current = current.getComponentType();
            }
            getDescriptor(stringBuilder, current);
        }

        stringBuilder.append(")");
        getDescriptor(stringBuilder, method.getReturnType());
        return stringBuilder.toString();
    }

    /**
     * Gets a descriptor from a class type and appends it to {@code stringBuilder}.
     *
     * @param stringBuilder The string builder.
     * @param current       The class.
     */
    private static void getDescriptor(StringBuilder stringBuilder, Class<?> current) {
        if (current.isPrimitive()) {
            switch (current.getTypeName()) {
                case "void":
                    stringBuilder.append("V");
                    break;
                case "boolean":
                    stringBuilder.append("Z");
                    break;
                case "byte":
                    stringBuilder.append("B");
                    break;
                case "short":
                    stringBuilder.append("S");
                    break;
                case "char":
                    stringBuilder.append("C");
                    break;
                case "int":
                    stringBuilder.append("I");
                    break;
                case "float":
                    stringBuilder.append("F");
                    break;
                case "double":
                    stringBuilder.append("D");
                    break;
                case "long":
                    stringBuilder.append("J");
            }
        } else {
            stringBuilder.append("L").append(current.getName().replace(".", "/")).append(";");
        }
    }

    public DirectInvokerClassLoader getClassLoader() {
        return classLoader;
    }
}
