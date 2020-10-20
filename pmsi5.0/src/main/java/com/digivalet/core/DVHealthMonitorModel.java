package com.digivalet.core;

public class DVHealthMonitorModel
{
   private boolean serviceStatus;
   private boolean databaseStatus;
   private boolean pmsConnectionStatus;
   private boolean pmsLinkStatus;
   private long timestamp;

   public boolean isServiceStatus()
   {
      return serviceStatus;
   }

   public void setServiceStatus(boolean serviceStatus)
   {
      this.serviceStatus = serviceStatus;
   }

   public boolean isDatabaseStatus()
   {
      return databaseStatus;
   }

   public void setDatabaseStatus(boolean databaseStatus)
   {
      this.databaseStatus = databaseStatus;
   }

   public long getTimestamp()
   {
      return timestamp;
   }

   public void setTimestamp(long timestamp)
   {
      this.timestamp = timestamp;
   }

   public boolean isPmsConnectionStatus()
   {
      return pmsConnectionStatus;
   }

   public void setPmsConnectionStatus(boolean pmsConnectionStatus)
   {
      this.pmsConnectionStatus = pmsConnectionStatus;
   }

   public boolean isPmsLinkStatus()
   {
      return pmsLinkStatus;
   }

   public void setPmsLinkStatus(boolean pmsLinkStatus)
   {
      this.pmsLinkStatus = pmsLinkStatus;
   }

   @Override
   public String toString()
   {
      return "DVHealthMonitorModel [serviceStatus=" + serviceStatus
               + ", databaseStatus=" + databaseStatus + ", pmsConnectionStatus="
               + pmsConnectionStatus + ", pmsLinkStatus=" + pmsLinkStatus
               + ", timestamp=" + timestamp + "]";
   }

}
