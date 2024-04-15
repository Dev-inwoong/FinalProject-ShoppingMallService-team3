package kr.co.lion.farming_customer

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.drawable.BitmapDrawable
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.os.SystemClock
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import java.io.FileOutputStream
import android.app.Activity
import kotlin.concurrent.thread

class Tools {
    companion object{
        ///// 카메라, 앨범 공통 ////////
        // 사진의 회전 각도값을 반환하는 메서드
        // ExifInterface : 사진, 영상, 소리 등의 파일에 기록한 정보
        // 위치, 날짜, 조리개값, 노출 정도 등등 다양한 정보가 기록된다.
        // ExifInterface 정보에서 사진 회전 각도값을 가져와서 그만큼 다시 돌려준다.
        fun getDegree(context: Context, uri: Uri) : Int {
            // 사진 정보를 가지고 있는 객체 가져온다.
            var exifInterface: ExifInterface? = null


            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
                // 이미지 데이터를 가져올 수 있는 Content Provide의 Uri를 추출한다.
                // val photoUri = MediaStore.setRequireOriginal(uri)
                // ExifInterface 정보를 읽어올 스트림을 추출한다.

                val inputStream = context.contentResolver.openInputStream(uri)!!
                // ExifInterface 객체를 생성한다.
                exifInterface = ExifInterface(inputStream)
            } else {
                // ExifInterface 객체를 생성한다.
                exifInterface = ExifInterface(uri.path!!)
            }

            if(exifInterface != null){
                // 반환할 각도값을 담을 변수
                var degree = 0
                // ExifInterface 객체에서 회전 각도값을 가져온다.
                val ori = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, -1)

                degree = when(ori){
                    ExifInterface.ORIENTATION_ROTATE_90 -> 90
                    ExifInterface.ORIENTATION_ROTATE_180 -> 180
                    ExifInterface.ORIENTATION_ROTATE_270 -> 270
                    else -> 0
                }

                return degree
            }

            return 0
        }

        // 회전시키는 메서드
        fun rotateBitmap(bitmap: Bitmap, degree:Float): Bitmap {
            // 회전 이미지를 생성하기 위한 변환 행렬
            val matrix = Matrix()
            matrix.postRotate(degree)

            // 회전 행렬을 적용하여 회전된 이미지를 생성한다.
            // 첫 번째 : 원본 이미지
            // 두 번째와 세번째 : 원본 이미지에서 사용할 부분의 좌측 상단 x, y 좌표
            // 네번째와 다섯번째 : 원본 이미지에서 사용할 부분의 가로 세로 길이
            // 여기에서는 이미지데이터 전체를 사용할 것이기 때문에 전체 영역으로 잡아준다.
            // 여섯번째 : 변환행렬. 적용해준 변환행렬이 무엇이냐에 따라 이미지 변형 방식이 달라진다.
            val rotateBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, false)

            return rotateBitmap
        }

        // 이미지 사이즈를 조정하는 메서드
        fun resizeBitmap(bitmap: Bitmap, targetWidth:Int): Bitmap {
            // 이미지의 확대/축소 비율을 구한다.
            val ratio = targetWidth.toDouble() / bitmap.width.toDouble()
            // 세로 길이를 구한다.
            val targetHeight = (bitmap.height * ratio).toInt()
            // 크기를 조장한 Bitmap을 생성한다.
            val resizedBitmap = Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, false)

            return resizedBitmap
        }

        // 이미지뷰의 이미지를 추출해 로컬에 저장한다.
        fun saveImageViewData(context: Context, imageView: ImageView, fileName:String){
            // 외부 저장소까지의 경로를 가져온다.
            val filePath = context.getExternalFilesDir(null).toString()
            // 이미지 뷰에서 BitmapDrawable 객체를 추출한다.
            val bitmapDrawable = imageView.drawable as BitmapDrawable

            // 로컬에 저장할 경로
            val file = File("${filePath}/${fileName}")
            // 스트림 추출
            val fileOutputStream = FileOutputStream(file)
            // 이미지를 저장한다.
            // 첫 번째 : 이미지 데이터 포멧(JPEG, PNG, WEBP_LOSSLESS, WEBP_LOSSY)
            // 두 번째 : 이미지의 퀄리티
            // 세 번째 : 이미지 데이터를 저장할 파일과 연결된 스트림
            bitmapDrawable.bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream)
            fileOutputStream.flush()
            fileOutputStream.close()
        }
        // 뷰에 포커스를 주고 키보드를 올린다.
        fun showSoftInput(context: Context, view: View) {
            // 뷰에 포커스를 준다.
            view.requestFocus()
            thread {
                // 딜레이
                SystemClock.sleep(200)
                // 키보드 관리 객체를 가져온다.
                val inputMethodManger =
                    context.getSystemService(AppCompatActivity.INPUT_METHOD_SERVICE) as InputMethodManager
                // 키보드를 올린다.
                inputMethodManger.showSoftInput(view, 0)
            }
        }

        // 키보드를 내려주고 포커스를 제거한다.
        fun hideSoftInput(activity: Activity){
            // 포커스를 가지고 있는 뷰가 있다면..
            if(activity.window.currentFocus != null){
                // 키보드 관리 객체를 가져온다.
                val inputMethodManger = activity.getSystemService(AppCompatActivity.INPUT_METHOD_SERVICE) as InputMethodManager
                // 키보드를 내려준다.
                inputMethodManger.hideSoftInputFromWindow(activity.window.currentFocus?.windowToken, 0)
                // 포커스를 제거해준다.
                activity.window.currentFocus?.clearFocus()
            }
        }
    }

}

// MainActivity에서 보여줄 프래그먼트들의 이름
enum class MainFragmentName(var str : String){
    TRADE_FRAGMENT("TradeFragment"),
    HOME_FRAGMENT("HomeFragment"),
    LIKE_FRAGMENT("LikeFragment"),
    MY_PAGE_FRAGMENT("MyPageFragment"),
    COMMUNITY_FRAGMENT("CommunityFragment"),
    FARMING_LIFE_FARM_AND_ACTIVITY_FRAGMENT("FarmingLifeFarmAndActivityFragment"),
    TAP_FARM_FRAGMENT("TapFarmFragment"),
    TAP_ACTIVITY_FRAGMENT("TapActivityFragment"),
    FARMING_LIFE_TOOLS_FRAGMENT("FarmingLifeToolsFragment")
}

// PointActivity에서 보여줄 프래그먼트들의 이름
enum class PointFragmentName(var str: String) {
    POINT_HISTORY_FRAGMENT("PointHistoryFragment")
}

// ReviewActivity에서 보여줄 프래그먼트들의 이름
enum class ReviewFragmentName(var str: String) {
    REVIEW_HISTORY_FRAGMENT("ReviewHistoryFragment"),
    REVIEW_TAB_CROP_FRAGMENT("ReviewTabCropFragment"),
    REVIEW_TAB_FARM_FRAGMENT("ReviewTabFarmFragment"),
    REVIEW_TAB_ACTIVITY_FRAGMENT("ReviewTabActivityFragment")
}

// CartActivity에서 보여줄 프래그먼트들의 이름
enum class CartFragmentName(var str: String) {
    CART_FRAGMENT("CartFragment"),
    CART_TAB_CROP_FRAGMENT("CartTabCropFragment"),
    CART_TAB_FARM_FRAGMENT("CartTabFarmFragment"),
    CART_TAB_ACTIVITY_FRAGMENT("CartTabActivityFragment")
}


// CommunityFragment에서 보여줄 프래그먼트들의 이름
enum class CommunityTabFragmentName(var str: String) {
    COMMUNITY_TAB_ALL_FRAGMENT("CommunityTabAllFragment"),
    COMMUNITY_TAB_INFORMATION_FRAGMENT("CommunityTabInformationFragment"),
    COMMUNITY_TAB_SOCIAL_FRAGMENT("CommunityTabSocialFragment"),
    COMMUNITY_TAB_JOB_FRAGMENT("CommunityTabJobFragment")
}

// CommunityActivity에서 보여줄 프래그먼트들의 이름
enum class CommunityFragmentName(var str: String) {
    COMMUNITY_READ_FRAGMENT("CommunityReadFragment"),
    COMMUNITY_MODIFY_FRAGMENT("CommunityModifyFragment")
}

// CommunityAddActivity에서 보여줄 프래그먼트들의 이름
enum class CommunityAddFragmentName(var str: String) {
    COMMUNITY_ADD_FRAGMENT("CommunityAddFragment")
}

// CommunitySearchActivity에서 보여줄 프래그먼트들의 이름
enum class CommunitySearchFragmentName(var str: String) {
    COMMUNITY_SEARCH_FRAGMENT("CommunitySearchFragment")
}

// 좋아요 타입
enum class LikeType(var str:String, var num:Int){
    CROP("Crop",1),
    POST("Post",2),
    FARM("Farm",3),
    ACTIVITY("Activity",4),
    RENTAL("Rental",5)
}

// MyPageManagementActivity에서 보여줄 프래그먼트들의 이름
enum class MyPageManagementName(var str:String){
    MY_PAGE_MANAGEMENT_MAIN("MyPageManagementMainFragment"),
    MY_PAGE_MANAGEMENT_PROFILE_MODIFY("MyPageManagementProfileModifyFragment"),
    MY_PAGE_MANAGEMENT_CHECK_USER_INFO_MODIFY("MyPageManagementCheckUserInfoModifyFragment"),
    MY_PAGE_MANAGEMENT_USER_INFO_MODIFY("MyPageManagementUserInfoModifyFragment"),
    MY_PAGE_MANAGEMENT_DELIVERY_ADDRESS("MyPageManagementDeliveryAddressFragment"),
    MY_PAGE_MANAGEMENT_ADD_DELIVERY_ADDRESS("MyPageManagementAddDeliveryAddressFragment"),
    MY_PAGE_MANAGEMENT_MODIFY_DELIVERY_ADDRESS("MyPageManagementModifyDeliveryAddressFragment")
}

enum class MyPageServiceCenterFragmentName(var str: String) {
    SERVICE_CENTER_NOTICE_FRAGMENT("MyPageServiceCenterNoticeFragment"),
    SERVICE_CENTER_FAQ_FRAGMENT("MyPageServiceCenterFaqFragment"),
    SERVICE_CENTER_INQUIRY_FRAGMENT("MyPageServiceCenterInquiryFragment")
}

// OrderHistoryActivity에서 보여줄 프래그먼트들의 이름
enum class OrderHistoryFragmentName(var str:String){
    ORDER_HISTORY_CROP_FRAGMENT("OrderHistoryCropFragment"),
    ORDER_HISTORY_FARM_FRAMGNET("OrderHistoryFarmFragment"),
    ORDER_HISTORY_ACTIVITY_FRAGMENT("OrderHistoryActivityFragment"),
    TAP_DELIVERY_DONE_FRAGMENT("TapDeliveryDoneFragment"),
    TAP_DELIVERY_FRAGMENT("TapDeliveryFragment"),
    TAP_PAYMENT_DONE_FRAGMENT("TapPaymentDoneFragment"),
    TAP_RESERV_DONE_FRAGMENT("TapReservDoneFragment"),
    TAP_RESERV_CANCLE_FRAGMENT("TapReservCancleFragment"),
    ORDER_HISTORY_RESERV_CANCLE_FRAGMENT("OrderHistoryReservCancleFragment"),
    ORDER_HISTORY_RESERV_DETAIL_FRAGMENT("OrderHistoryReservDetailFragment"),
    ORDER_HISTORY_ORDER_DETAIL_FRAGMENT("OrderHistoryOrderDetailFragment"),
    ORDER_HISTORY_EXCHANGE_RETURN_FRAGMENT("OrderHistoryExchangeReturnFragment"),
    ORDER_HISTORY_WRITE_REVIEW_FRAGMENT("OrderHistoryWriteReviewFragment")
}

enum class FarmingLifeFragmnetName(var str:String){
    FARMING_LIFE_SEARCH_FRAGMENT("FarmingLifeSearchFragment"),
    FARMING_LIFE_FARM_DETAIL_FARMGNET("FarmingLifeFarmDetailFragment"),
    FARMING_LIFE_ACTIVITY_DETAIL_FRAGMENT("FarmingLifeActivityDetailFragment")
}
// 농기구 프래그먼트
enum class FarmingLifeToolsFragmentName(var str: String) {
    FARMING_LIFE_TOOLS_MAP_FRAGMENT("FarmingLifeToolsMapFragment"),
    FARMING_LIFE_TOOLS_LIST_FRAGMENT("FarmingLifeToolsListFragment")
}

enum class LoginFragmentName(var str: String){
    LOGIN_FRAGMENT("LoginFragment"),
    REGISTER_FRAGMENT("RegisterFragment"),
    REGISTER2_FRAGMENT("Register2Fragment"),
    REGISTER3_FRAGMENT("Register3Fragment"),
    FIND_ACCOUNT_FRAGMENT("FindAccountFragment"),
    FIND_ID_DONE_FRAGMENT("FindIdDoneFragment"),
    FIND_PW_DONE_FRAGMENT("FindPwDoneFragment"),
    FIND_PW_DONE2_FRAGMENT("FindPwDone2Fragment"),
}

// PaymentActivity에서 보여줄 프래그먼트들의 이름
enum class PaymentFragmentName(var str:String){
    PAYMENT_CROP_FRAGMENT("PaymentCropFragment"),
    PAYMENT_FARM_ACTIVITY_FRAGMENT("PaymentFarmActivityFragment"),
    PAYMENT_SUCCESS_FRAGMENT("PaymentSuccessFragment"),
    PAYMENT_FAIL_FRAGMENT("PaymentFailFragment"),
    PAYMENT_DELIVERY_ADDRESS_FRAGMENT("PaymentDeliveryAddressFragment")
}