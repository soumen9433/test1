package com.digivalet.core;

import java.io.IOException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class DVPms extends HttpServlet
{

   /**
    * 
    */
   private static final long serialVersionUID = 1L;
   
   
   public void init(ServletConfig config) throws ServletException 
   {

      try
      {
         DVPmsMain.initialize();
      }
      catch (Exception  e)
      {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
   }

  /**
   * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
   *      response)
   */
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
          throws ServletException, IOException {
     
  }

  /**
   * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
   *      response)
   */
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
          throws ServletException, IOException {
     
  }


}