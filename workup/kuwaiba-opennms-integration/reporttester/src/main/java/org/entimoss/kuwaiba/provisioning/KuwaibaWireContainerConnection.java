package org.entimoss.kuwaiba.provisioning;

public class KuwaibaWireContainerConnection {
   
   private KuwaibaClass connectionClass;
   
   private KuwaibaClass aEnd;
   
   private KuwaibaClass bEnd;

   public KuwaibaWireContainerConnection() {
      super();
   }

   public KuwaibaClass getConnectionClass() {
      return connectionClass;
   }

   public void setConnectionClass(KuwaibaClass connectionClass) {
      this.connectionClass = connectionClass;
   }

   public KuwaibaClass getaEnd() {
      return aEnd;
   }

   public void setaEnd(KuwaibaClass aEnd) {
      this.aEnd = aEnd;
   }

   public KuwaibaClass getbEnd() {
      return bEnd;
   }

   public void setbEnd(KuwaibaClass bEnd) {
      this.bEnd = bEnd;
   }

   @Override
   public String toString() {
      return "KuwaibaWireContainerConnection [connectionClass=" + connectionClass + ", aEnd=" + aEnd + ", bEnd=" + bEnd + "]";
   }

}
