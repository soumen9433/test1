package com.digivalet.pmsi.mews;

enum MewsKeyTags
{
   EVENTS("Events"),

   ASSIGNEDSPACEID("AssignedSpaceId"),

   STARTUTC("StartUtc"),

   ENDUTC("EndUtc"),

   CUSTOMERS("Customers"),

   ID("Id"),

   STATE("State"),
   
   TYPE("Type"),

   CUSTOMERID("CustomerId"),

   COMPANIONIDS("CompanionIds"),

   RESERVATIONS("Reservations"),

   SPACES("Spaces"),

   GROUPID("GroupId"),

   BIRTHDATE("BirthDate"),

   NUMBER("Number");

   private String value;

   MewsKeyTags(String value)
   {
      this.value = value;
   }

   @Override
   public String toString()
   {
      return String.valueOf(value);
   }

   public static MewsKeyTags fromValue(String text)
   {
      for (MewsKeyTags b : MewsKeyTags.values())
      {
         if (String.valueOf(b.value).equals(text))
         {
            return b;
         }
      }
      return null;
   }
}


enum EventEnums
{
   RESERVATION,

   SPACE,

   COMMAND
}


enum EventState
{
   STARTED,

   CONFIRMED,

   PROCESSED
}


enum ClassificationTags
{
   RETURNING,

   VeryImportant
}


enum GuestData
{
   guestType,

   PRIMARY,

   SECONDARY,

   ReservationId
}


enum BillTags
{
   Bills,

   Revenue,

   BillId,

   Amount,

   Currency,

   Value,

   Notes,

   ConsumptionUtc,

   Name;
}
