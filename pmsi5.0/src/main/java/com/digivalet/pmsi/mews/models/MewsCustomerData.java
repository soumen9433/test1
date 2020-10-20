package com.digivalet.pmsi.mews.models;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class MewsCustomerData
{
   @SerializedName("Id")
   @Expose
   private String id;
   @SerializedName("Number")
   @Expose
   private String number;
   @SerializedName("Title")
   @Expose
   private String title;
   @SerializedName("Gender")
   @Expose
   private String gender;
   @SerializedName("FirstName")
   @Expose
   private String firstName;
   @SerializedName("LastName")
   @Expose
   private String lastName;
   @SerializedName("SecondLastName")
   @Expose
   private String secondLastName;
   @SerializedName("NationalityCode")
   @Expose
   private String nationalityCode;
   @SerializedName("LanguageCode")
   @Expose
   private String languageCode;
   @SerializedName("BirthDate")
   @Expose
   private String birthDate;
   @SerializedName("BirthPlace")
   @Expose
   private String birthPlace;
   @SerializedName("CitizenNumber")
   @Expose
   private String citizenNumber;
   @SerializedName("MotherName")
   @Expose
   private String motherName;
   @SerializedName("FatherName")
   @Expose
   private String fatherName;
   @SerializedName("Occupation")
   @Expose
   private String occupation;
   @SerializedName("Email")
   @Expose
   private String email;
   @SerializedName("Phone")
   @Expose
   private String phone;
   @SerializedName("TaxIdentificationNumber")
   @Expose
   private String taxIdentificationNumber;
   @SerializedName("LoyaltyCode")
   @Expose
   private String loyaltyCode;
   @SerializedName("Notes")
   @Expose
   private String notes;
   @SerializedName("CreatedUtc")
   @Expose
   private String createdUtc;
   @SerializedName("UpdatedUtc")
   @Expose
   private String updatedUtc;
   @SerializedName("Passport")
   @Expose
   private Object passport;
   @SerializedName("IdentityCard")
   @Expose
   private Object identityCard;
   @SerializedName("Visa")
   @Expose
   private Object visa;
   @SerializedName("DriversLicense")
   @Expose
   private Object driversLicense;
   @SerializedName("Address")
   @Expose
   private Address address;
   @SerializedName("Classifications")
   @Expose
   private List<String> classifications = null;
   @SerializedName("Options")
   @Expose
   private List<Object> options = null;
   @SerializedName("CategoryId")
   @Expose
   private String categoryId;
   @SerializedName("BirthDateUtc")
   @Expose
   private String birthDateUtc;

   public String getId()
   {
      return id;
   }

   public void setId(String id)
   {
      this.id = id;
   }

   public String getNumber()
   {
      return number;
   }

   public void setNumber(String number)
   {
      this.number = number;
   }

   public String getTitle()
   {
      return title;
   }

   public void setTitle(String title)
   {
      this.title = title;
   }

   public String getGender()
   {
      return gender;
   }

   public void setGender(String gender)
   {
      this.gender = gender;
   }

   public String getFirstName()
   {
      return firstName;
   }

   public void setFirstName(String firstName)
   {
      this.firstName = firstName;
   }

   public String getLastName()
   {
      return lastName;
   }

   public void setLastName(String lastName)
   {
      this.lastName = lastName;
   }

   public String getSecondLastName()
   {
      return secondLastName;
   }

   public void setSecondLastName(String secondLastName)
   {
      this.secondLastName = secondLastName;
   }

   public String getNationalityCode()
   {
      return nationalityCode;
   }

   public void setNationalityCode(String nationalityCode)
   {
      this.nationalityCode = nationalityCode;
   }

   public String getLanguageCode()
   {
      return languageCode;
   }

   public void setLanguageCode(String languageCode)
   {
      this.languageCode = languageCode;
   }

   public String getBirthDate()
   {
      return birthDate;
   }

   public void setBirthDate(String birthDate)
   {
      this.birthDate = birthDate;
   }

   public String getBirthPlace()
   {
      return birthPlace;
   }

   public void setBirthPlace(String birthPlace)
   {
      this.birthPlace = birthPlace;
   }

   public String getCitizenNumber()
   {
      return citizenNumber;
   }

   public void setCitizenNumber(String citizenNumber)
   {
      this.citizenNumber = citizenNumber;
   }

   public String getMotherName()
   {
      return motherName;
   }

   public void setMotherName(String motherName)
   {
      this.motherName = motherName;
   }

   public String getFatherName()
   {
      return fatherName;
   }

   public void setFatherName(String fatherName)
   {
      this.fatherName = fatherName;
   }

   public String getOccupation()
   {
      return occupation;
   }

   public void setOccupation(String occupation)
   {
      this.occupation = occupation;
   }

   public String getEmail()
   {
      return email;
   }

   public void setEmail(String email)
   {
      this.email = email;
   }

   public String getPhone()
   {
      return phone;
   }

   public void setPhone(String phone)
   {
      this.phone = phone;
   }

   public String getTaxIdentificationNumber()
   {
      return taxIdentificationNumber;
   }

   public void setTaxIdentificationNumber(String taxIdentificationNumber)
   {
      this.taxIdentificationNumber = taxIdentificationNumber;
   }

   public String getLoyaltyCode()
   {
      return loyaltyCode;
   }

   public void setLoyaltyCode(String loyaltyCode)
   {
      this.loyaltyCode = loyaltyCode;
   }

   public String getNotes()
   {
      return notes;
   }

   public void setNotes(String notes)
   {
      this.notes = notes;
   }

   public String getCreatedUtc()
   {
      return createdUtc;
   }

   public void setCreatedUtc(String createdUtc)
   {
      this.createdUtc = createdUtc;
   }

   public String getUpdatedUtc()
   {
      return updatedUtc;
   }

   public void setUpdatedUtc(String updatedUtc)
   {
      this.updatedUtc = updatedUtc;
   }

   public Object getPassport()
   {
      return passport;
   }

   public void setPassport(Object passport)
   {
      this.passport = passport;
   }

   public Object getIdentityCard()
   {
      return identityCard;
   }

   public void setIdentityCard(Object identityCard)
   {
      this.identityCard = identityCard;
   }

   public Object getVisa()
   {
      return visa;
   }

   public void setVisa(Object visa)
   {
      this.visa = visa;
   }

   public Object getDriversLicense()
   {
      return driversLicense;
   }

   public void setDriversLicense(Object driversLicense)
   {
      this.driversLicense = driversLicense;
   }

   public Address getAddress()
   {
      return address;
   }

   public void setAddress(Address address)
   {
      this.address = address;
   }

   public List<String> getClassifications()
   {
      return classifications;
   }

   public void setClassifications(List<String> classifications)
   {
      this.classifications = classifications;
   }

   public List<Object> getOptions()
   {
      return options;
   }

   public void setOptions(List<Object> options)
   {
      this.options = options;
   }

   public String getCategoryId()
   {
      return categoryId;
   }

   public void setCategoryId(String categoryId)
   {
      this.categoryId = categoryId;
   }

   public String getBirthDateUtc()
   {
      return birthDateUtc;
   }

   public void setBirthDateUtc(String birthDateUtc)
   {
      this.birthDateUtc = birthDateUtc;
   }


}


class Address
{

   @SerializedName("Line1")
   @Expose
   private String line1;
   @SerializedName("Line2")
   @Expose
   private String line2;
   @SerializedName("City")
   @Expose
   private String city;
   @SerializedName("PostalCode")
   @Expose
   private String postalCode;
   @SerializedName("CountryCode")
   @Expose
   private String countryCode;
   @SerializedName("CountrySubdivisionCode")
   @Expose
   private String countrySubdivisionCode;

   public String getLine1()
   {
      return line1;
   }

   public void setLine1(String line1)
   {
      this.line1 = line1;
   }

   public String getLine2()
   {
      return line2;
   }

   public void setLine2(String line2)
   {
      this.line2 = line2;
   }

   public String getCity()
   {
      return city;
   }

   public void setCity(String city)
   {
      this.city = city;
   }

   public String getPostalCode()
   {
      return postalCode;
   }

   public void setPostalCode(String postalCode)
   {
      this.postalCode = postalCode;
   }

   public String getCountryCode()
   {
      return countryCode;
   }

   public void setCountryCode(String countryCode)
   {
      this.countryCode = countryCode;
   }

   public String getCountrySubdivisionCode()
   {
      return countrySubdivisionCode;
   }

   public void setCountrySubdivisionCode(String countrySubdivisionCode)
   {
      this.countrySubdivisionCode = countrySubdivisionCode;
   }

}

