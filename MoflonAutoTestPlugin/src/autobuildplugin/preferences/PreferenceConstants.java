package autobuildplugin.preferences;

/**
 * Constant definitions for plug-in preferences
 */
public class PreferenceConstants
{

   public static final String AB_PROJECTSET = "projectSetPreference";
   public static final String AB_DEFAULT_PROJECTSET = "<Path to PSF file>/<PSF file>.psf";
   public static final String AB_LOG = "log";
   public static final String AB_LOG_PATH = "<Store Log in Path>";

   public static final String AB_NEXTOP = "nextOPPreference";
   public static final String AB_DEFAULT_NEXTOP = NEXTOPERATION.IMPORTPROJECTSET.toString();
   
   public static enum NEXTOPERATION {
      DELETEWORKSPACE {
         public String toString()
         {
            return "deleteworkspace";
         }
      },
      IMPORTPROJECTSET {
         public String toString()
         {
            return "importprojectset";
         }
      },
      REFRESH {
         public String toString()
         {
            return "refreshworkspace";
         }
      },
      JUNIT {
         public String toString()
         {
            return "junit";
         }
      },
      MOFLONBUILD{
         public String toString()
         {
            return "moflonbuild";
         }
      }
   };
}
