CL_YLW="\033[1;33m"
CL_GRN="\033[1;32m"
CL_RST="\033[0m"

  read -rp "If you like to compile GApps Build choose {y} or you want Vanilla Build choose {n}:" choice

  case ${choice} in
  Y | y) export WITH_GAPPS=true && export TARGET_GAPPS_ARCH=arm64 && export TARGET_INCLUDE_LIVE_WALLPAPERS=true && echo -e ${CL_GRN}"OK! Your Build include GApps packages"${CL_RST} ;;
  N | n) export WITH_GAPPS=false && export TARGET_INCLUDE_LIVE_WALLPAPERS=false && echo -e ${CL_YLW}"OK! Your Build include non-GApps packages"${CL_RST} ;;
  esac
