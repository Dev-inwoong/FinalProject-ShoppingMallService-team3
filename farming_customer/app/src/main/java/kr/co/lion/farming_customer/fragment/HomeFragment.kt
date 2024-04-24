package kr.co.lion.farming_customer.fragment

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.divider.MaterialDividerItemDecoration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kr.co.lion.farming_customer.FarmingLifeFragmnetName
import kr.co.lion.farming_customer.PointStatus
import kr.co.lion.farming_customer.R
import kr.co.lion.farming_customer.activity.CommunityActivity
import kr.co.lion.farming_customer.activity.MainActivity
import kr.co.lion.farming_customer.activity.farmingLife.FarmingLifeActivity
import kr.co.lion.farming_customer.dao.farmingLife.ActivityDao
import kr.co.lion.farming_customer.dao.farmingLife.FarmDao
import kr.co.lion.farming_customer.activity.tradeCrop.TradeDetailActivity
import kr.co.lion.farming_customer.dao.crop.CropDao
import kr.co.lion.farming_customer.dao.myPagePoint.myPagePointDao
import kr.co.lion.farming_customer.databinding.FragmentHomeBinding
import kr.co.lion.farming_customer.databinding.ItemProductBinding
import kr.co.lion.farming_customer.databinding.RowCommunityTabAllBinding
import kr.co.lion.farming_customer.databinding.RowGridItemBinding
import kr.co.lion.farming_customer.databinding.RowLikeCropBinding
import kr.co.lion.farming_customer.databinding.RowRelatedCropBinding
import kr.co.lion.farming_customer.model.CropModel
import kr.co.lion.farming_customer.viewmodel.CommunityViewModel
import kr.co.lion.farming_customer.viewmodel.HomeViewModel
import kr.co.lion.farming_customer.viewmodel.farmingLife.RowGridItemViewModel
import kr.co.lion.farming_customer.viewmodel.tradeCrop.TradeViewModel
import kr.co.lion.farming_customer.model.farmingLife.ActivityModel
import kr.co.lion.farming_customer.model.farmingLife.FarmModel


class HomeFragment : Fragment() {
    lateinit var fragmentHomeBinding: FragmentHomeBinding
    lateinit var mainActivity: MainActivity

    lateinit var homeViewModel: HomeViewModel

    // 추천 농산물 데이터를 담을 리스트
    var cropLikeTop5List = listOf<CropModel>()
    
    var farmAndActivityList = mutableListOf<Any>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        fragmentHomeBinding = DataBindingUtil.inflate(layoutInflater, R.layout.fragment_home, container, false)
        homeViewModel = HomeViewModel()
        fragmentHomeBinding.homeViewModel = homeViewModel
        fragmentHomeBinding.lifecycleOwner = this
        mainActivity = activity as MainActivity
        // gettingCropLikeTop5()


        settingData()
        settingRecyclerView()


        return fragmentHomeBinding.root
    }

    private fun settingData() {
        CoroutineScope(Dispatchers.Main).launch {
            // 주말농장 데이터를 가져온다.
            val farmList = FarmDao.gettingFarmListOrderByLikeCnt()
            // 체험활동 데이터를 가져온다.
            val activityList = ActivityDao.gettingActivityListOrderByLikeCnt()
            // 추천 농산물 데이터를 가져온다.
            cropLikeTop5List = CropDao.gettingCropLikeTop5List()

            var index = 0
            while (farmAndActivityList.size < 6){
                if(farmList[index].farm_like_cnt > activityList[index].activity_like_cnt){
                    farmAndActivityList.add(farmList[index])
                }else if(farmList[index].farm_like_cnt == activityList[index].activity_like_cnt){
                    farmAndActivityList.add(farmList[index])
                    farmAndActivityList.add(activityList[index])
                }else{
                    farmAndActivityList.add(activityList[index])
                }
                index ++
            }
            fragmentHomeBinding.viewPagerFarm.adapter?.notifyDataSetChanged()
        }
    }

    private fun settingRecyclerView() {
        fragmentHomeBinding.apply {
            // 농산물 뷰 페이저 어댑터
            viewPagerCrop.apply {
                adapter = ViewPagerCropAdapter()
            }

            // 주말농장 / 체험활동 뷰페이저 어댑터
            viewPagerFarm.apply {
                adapter = ViewPagerFarmAdapter()
            }

            // 커뮤니티 리사이클러뷰 어댑터
            recyclerViewBoard.apply {
                adapter = BoardRecyClerViewAdatper()
                layoutManager = LinearLayoutManager(mainActivity)
                addItemDecoration(MaterialDividerItemDecoration(requireContext(), MaterialDividerItemDecoration.VERTICAL))
            }
        }
    }

    // 추천 농산물 뷰 페이저 어댑터
    inner class ViewPagerCropAdapter : RecyclerView.Adapter<ViewPagerCropAdapter.ViewPagerCropViewHolder>(){
        inner class ViewPagerCropViewHolder(itemProductBinding: ItemProductBinding) : RecyclerView.ViewHolder(itemProductBinding.root){
            val itemProductBinding : ItemProductBinding
            init {
                this.itemProductBinding = itemProductBinding

                itemProductBinding.root.layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewPagerCropViewHolder {
            val itemProductBinding = DataBindingUtil.inflate<ItemProductBinding>(layoutInflater, R.layout.item_product, parent, false)
            val tradeViewModel = TradeViewModel()
            itemProductBinding.tradeViewModel = tradeViewModel
            itemProductBinding.lifecycleOwner = this@HomeFragment

            val viewPagerCropViewHolder = ViewPagerCropViewHolder(itemProductBinding)
            return viewPagerCropViewHolder
        }

        override fun getItemCount(): Int {
            return cropLikeTop5List.size
        }

        override fun onBindViewHolder(holder: ViewPagerCropViewHolder, position: Int) {
            holder.itemProductBinding.tradeViewModel!!.apply {

                textViewCropName.value = cropLikeTop5List[position].crop_title
                textViewTradePrice.value = cropLikeTop5List[position].crop_option_detail[0]["crop_option_price"]
                textViewTradeLike.value = cropLikeTop5List[position].crop_like_cnt.toString()
                textViewCropLocation.value = cropLikeTop5List[position].crop_address
                holder.itemProductBinding.RatingBarTrade.rating = cropLikeTop5List[position].crop_rating.toFloat()
            }
            holder.itemProductBinding.root.setOnClickListener {
                val intent = Intent(mainActivity, TradeDetailActivity::class.java)
                intent.putExtra("crop_idx",cropLikeTop5List[position].crop_idx)
                startActivity(intent)
            }
            holder.itemProductBinding.tradeViewModel?.isLike?.value = false
            holder.itemProductBinding.imageButtonProductLike.setOnClickListener {
                if(holder.itemProductBinding.tradeViewModel?.isLike?.value!!){
                    holder.itemProductBinding.tradeViewModel?.isLike?.value = false
                    holder.itemProductBinding.imageButtonProductLike.setImageResource(R.drawable.heart_02)
                    holder.itemProductBinding.textViewTradeLike.setTextColor(ContextCompat.getColor(requireContext(), R.color.brown_01))

                }else{
                    holder.itemProductBinding.tradeViewModel?.isLike?.value = true
                    holder.itemProductBinding.imageButtonProductLike.setImageResource(R.drawable.heart_01)
                    holder.itemProductBinding.textViewTradeLike.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                }
            }
            CoroutineScope(Dispatchers.Main).launch {
                CropDao.gettingCropImage(mainActivity,cropLikeTop5List[position].crop_images[position],holder.itemProductBinding.imageViewCropImage)
            }
        }
    }

    // 주말 농장, 체험활동 뷰 페이저 어댑터
    inner class ViewPagerFarmAdapter : RecyclerView.Adapter<ViewPagerFarmAdapter.ViewPagerFarmViewHolder>(){
        inner class ViewPagerFarmViewHolder(rowGridItemBinding: RowGridItemBinding) : RecyclerView.ViewHolder(rowGridItemBinding.root){
            val rowGridItemBinding : RowGridItemBinding
            init {
                this.rowGridItemBinding = rowGridItemBinding
                rowGridItemBinding.root.layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            }

        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewPagerFarmViewHolder {
            val rowGridItemBinding = DataBindingUtil.inflate<RowGridItemBinding>(layoutInflater, R.layout.row_grid_item, parent, false)
            val rowGridItemViewModel = RowGridItemViewModel()
            rowGridItemBinding.rowGridItemViewModel = rowGridItemViewModel
            rowGridItemBinding.lifecycleOwner = this@HomeFragment

            val viewPagerFarmViewHolder = ViewPagerFarmViewHolder(rowGridItemBinding)
            return viewPagerFarmViewHolder
        }

        override fun getItemCount(): Int {
            return farmAndActivityList.size
        }

        override fun onBindViewHolder(holder: ViewPagerFarmViewHolder, position: Int) {
            holder.rowGridItemBinding.rowGridItemViewModel!!.apply {
                var model = farmAndActivityList[position]
                if(model is FarmModel){
                    textView_likeCnt.value = model.farm_like_cnt.toString()
                    textView_ItemName.value = model.farm_title
                    textView_location.value = model.farm_address
                    textView_price.value = model.farm_option_detail["price_area"].toString()
                }else if(model is ActivityModel){
                    textView_likeCnt.value = model.activity_like_cnt.toString()
                    textView_ItemName.value = model.activity_title
                    textView_location.value = model.activity_address

                    // 옵션 중 가장 최소 가격 표시
                    var minPrice = Int.MAX_VALUE
                    var minPrice_pos = -1
                    model.activity_option_detail.forEachIndexed { index, mutableMap ->
                        val priceString = mutableMap["option_price"] as String
                        val numberString = priceString.replace(",", "").replace("원", "")
                        val priceInt = numberString.toInt()

                        if(priceInt < minPrice){
                            minPrice = priceInt
                            minPrice_pos = index
                        }
                    }
                    textView_price.value = model.activity_option_detail[minPrice_pos]["option_price"] as String + " ~"
                }

                isLike.value = false
            }
            // 하트 클릭 리스너
            holder.rowGridItemBinding.apply {
                rowGridItemViewModel!!.apply {
                    constraintLikeCancel.setOnClickListener {
                        if(isLike.value!!){
                            isLike.value = false
                            imageViewHeart.setImageResource(R.drawable.heart_04)
                            textViewLikeCnt.setTextColor(ContextCompat.getColor(requireContext(), R.color.brown_01))

                        }else{
                            isLike.value = true
                            imageViewHeart.setImageResource(R.drawable.heart_01)
                            textViewLikeCnt.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                        }
                    }
                }
            }
            // 뷰페이저 아이템 클릭 리스너
            holder.rowGridItemBinding.root.setOnClickListener {
                val model = farmAndActivityList[position]
                // 주말농장인지 체험활동인지 구분해야함
                if(model is FarmModel){
                    val intent = Intent(mainActivity, FarmingLifeActivity::class.java)
                    intent.putExtra("fragmentName", FarmingLifeFragmnetName.FARMING_LIFE_FARM_DETAIL_FARMGNET)
                    intent.putExtra("idx", model.farm_idx)
                    startActivity(intent)
                }else if(model is ActivityModel){
                    val intent = Intent(mainActivity, FarmingLifeActivity::class.java)
                    intent.putExtra("fragmentName", FarmingLifeFragmnetName.FARMING_LIFE_ACTIVITY_DETAIL_FRAGMENT)
                    intent.putExtra("idx", model.activity_idx)
                    startActivity(intent)
                }

            }
            holder.rowGridItemBinding.apply {
                CoroutineScope(Dispatchers.Main).launch {
                    val model = farmAndActivityList[position]
                    if(model is FarmModel){
                        FarmDao.gettingFarmImage(mainActivity, model.farm_images[0], imageView)
                        ratingBar.rating = model.farm_star
                    }else if(model is ActivityModel){
                        ActivityDao.gettingActivityImage(mainActivity, model.activity_images[0], imageView)
                        ratingBar.rating = model.activity_star
                    }
                }
            }
        }
    }

    // 좋아요가 많은 게시판 리사이클러뷰 어댑터
    inner class BoardRecyClerViewAdatper : RecyclerView.Adapter<BoardRecyClerViewAdatper.BoardViewHolder>(){
        inner class BoardViewHolder(rowCommunityTabAllBinding: RowCommunityTabAllBinding) : RecyclerView.ViewHolder(rowCommunityTabAllBinding.root){
            val rowCommunityTabAllBinding : RowCommunityTabAllBinding
            init {
                this.rowCommunityTabAllBinding = rowCommunityTabAllBinding

                rowCommunityTabAllBinding.root.layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BoardViewHolder {
            val rowCommunityTabAllBinding = DataBindingUtil.inflate<RowCommunityTabAllBinding>(layoutInflater, R.layout.row_community_tab_all, parent, false)
            val communityViewModel = CommunityViewModel()
            rowCommunityTabAllBinding.communityViewModel = communityViewModel
            rowCommunityTabAllBinding.lifecycleOwner = this@HomeFragment

            val boardViewHolder = BoardViewHolder(rowCommunityTabAllBinding)
            return boardViewHolder
        }

        override fun getItemCount(): Int {
            return 5
        }

        override fun onBindViewHolder(holder: BoardViewHolder, position: Int) {
            holder.rowCommunityTabAllBinding.communityViewModel!!.apply {
                textViewCommunityListLabelAll.value = "전체"
                textViewCommunityListTitleAll.value = "글 제목"
                textViewCommunityListContentAll.value = "글 내용입니다. 글 내용입니다. 글 내용입니다. 글 내용입니다. 글 내용입니다. "
                textViewCommunityListViewCntAll.value = "99"
                textViewCommunityListCommentCntAll.value = "99"
                textViewCommunityListDateAll.value = "2024.04.12"
                textViewCommunityListLikeCntAll.value = "999"
                isLike.value = false
            }
            // 좋아요 클릭 리스너
            holder.rowCommunityTabAllBinding.apply {
                imageViewCommunityListLikeAll.setOnClickListener {
                    if(communityViewModel!!.isLike.value!!){
                        communityViewModel!!.isLike.value = false
                        imageViewCommunityListLikeAll.setImageResource(R.drawable.heart_02)
                        textViewCommunityListLikeCntAll.setTextColor(ContextCompat.getColor(requireContext(), R.color.brown_01))
                    }else{
                        communityViewModel!!.isLike.value = true
                        imageViewCommunityListLikeAll.setImageResource(R.drawable.heart_01)
                        textViewCommunityListLikeCntAll.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                    }
                }
            }
            // 아이템 클릭 리스너
            holder.rowCommunityTabAllBinding.root.setOnClickListener {
                val intent = Intent(mainActivity, CommunityActivity::class.java)
                startActivity(intent)
            }
        }
    }
}