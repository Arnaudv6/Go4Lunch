package com.freemyip.arnaudv6.go4lunch.data.pois

class PoiImages {

    companion object {
        private var lastImageIndex = 1
        private val imagesList = listOf(
            "https://www.themealdb.com/images/media/meals/md8w601593348504.jpg/preview",
            "https://www.themealdb.com/images/media/meals/hyarod1565090529.jpg/preview",
            "https://www.themealdb.com/images/media/meals/xxpqsy1511452222.jpg/preview",
            "https://www.themealdb.com/images/media/meals/yqqqwu1511816912.jpg/preview",
            "https://www.themealdb.com/images/media/meals/xvrrux1511783685.jpg/preview",
            "https://www.themealdb.com/images/media/meals/vqpwrv1511723001.jpg/preview",
            "https://www.themealdb.com/images/media/meals/t8mn9g1560460231.jpg/preview",
            "https://www.themealdb.com/images/media/meals/xrrwpx1487347049.jpg/preview",
            "https://www.themealdb.com/images/media/meals/rtwwvv1511799504.jpg/preview",
            "https://www.themealdb.com/images/media/meals/rjhf741585564676.jpg/preview",
            "https://www.themealdb.com/images/media/meals/kw92t41604181871.jpg/preview",
            "https://www.themealdb.com/images/media/meals/uquqtu1511178042.jpg/preview",
            "https://www.themealdb.com/images/media/meals/wvpvsu1511786158.jpg/preview",
            "https://www.themealdb.com/images/media/meals/yxsurp1511304301.jpg/preview",
            "https://www.themealdb.com/images/media/meals/rwuyqx1511383174.jpg/preview",
            "https://www.themealdb.com/images/media/meals/qtuwxu1468233098.jpg/preview",
            "https://www.themealdb.com/images/media/meals/lpd4wy1614347943.jpg/preview",
            "https://www.themealdb.com/images/media/meals/vytypy1511883765.jpg/preview",
            "https://www.themealdb.com/images/media/meals/xusqvw1511638311.jpg/preview",
            "https://www.themealdb.com/images/media/meals/svprys1511176755.jpg/preview",
            "https://www.themealdb.com/images/media/meals/1529446352.jpg/preview",
            "https://www.themealdb.com/images/media/meals/vrspxv1511722107.jpg/preview",
            "https://www.themealdb.com/images/media/meals/wxyvqq1511723401.jpg/preview",
            "https://www.themealdb.com/images/media/meals/x372ug1598733932.jpg/preview",
            "https://www.themealdb.com/images/media/meals/sytuqu1511553755.jpg/preview",
            "https://www.themealdb.com/images/media/meals/ryppsv1511815505.jpg/preview",
            "https://www.themealdb.com/images/media/meals/yuwtuu1511295751.jpg/preview",
            "https://www.themealdb.com/images/media/meals/k420tj1585565244.jpg/preview",
            "https://www.themealdb.com/images/media/meals/uwxqwy1483389553.jpg/preview",
            "https://www.themealdb.com/images/media/meals/tqrrsq1511723764.jpg/preview"
        )

        fun getImageUrl(): String {
            return imagesList[(lastImageIndex++) % imagesList.size]
        }
    }


}