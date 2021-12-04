package com.tonngtong.grammarproblemhandler;

/**
 * 推导过程，样例见resourcce下推导过程.png
 */
public class DerivationProcess {

    public LinkNode head;

    static class SignNode {
        public SignNode(char sign) {
            this.sign = sign;
        }
        public char sign;
        public SignNode next;
    }

    static class LinkNode {
        public LinkNode(SignNode headOfSentencePattern) {
            this.headOfSentencePattern = headOfSentencePattern;
        }

        public LinkNode() {
        }

        //句型首符号节点指针
        public SignNode headOfSentencePattern;
        //下一句型链头指针
        public LinkNode next;
    }

    public static LinkNode copyFrom(LinkNode source){
        LinkNode dest = new LinkNode();
        dest.headOfSentencePattern = new SignNode(source.headOfSentencePattern.sign);
        SignNode p = source.headOfSentencePattern;
        SignNode q = dest.headOfSentencePattern;
        while (p.next!=null){
            q.next = new SignNode(p.next.sign);
            q = q.next;
            p = p.next;
        }
        return dest;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        LinkNode lp = head;
        while (lp!=null){
            stringBuilder.append("-> ");
            SignNode np = lp.headOfSentencePattern;
            while (np!=null){
                stringBuilder.append(np.sign);
                np = np.next;
            }
            stringBuilder.append('\n');
            lp = lp.next;
        }
        return stringBuilder.toString();
    }
}
