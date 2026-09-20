package com.KAIIIAK.classManipulators.fakeNode;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.LabelNode;

import java.util.Map;

public class AnyNode extends AbstractInsnNode {
    
    public AnyNode() {
        super(-1);
    }

    @Override
    public int getType() {
        return -1;
    }

    @Override
    public void accept(MethodVisitor cv) {}

    @Override
    public AbstractInsnNode clone(Map<LabelNode, LabelNode> labels) {
        return new AnyNode();
    }
}
