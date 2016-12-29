package carton.fmy.com.yuanmanhua.activity;


import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemChildClickListener;
import com.ms.square.android.expandabletextview.ExpandableTextView;

import java.util.List;

import carton.fmy.com.yuanmanhua.R;
import carton.fmy.com.yuanmanhua.adapter.CataloguAdapter;
import carton.fmy.com.yuanmanhua.bean.CatalogueBean;
import carton.fmy.com.yuanmanhua.url.UrlCatalogueInterface;
import carton.fmy.com.yuanmanhua.utils.CollectUtilt;
import carton.fmy.com.yuanmanhua.utils.DialogUtil;
import carton.fmy.com.yuanmanhua.utils.NetUtil;
import carton.fmy.com.yuanmanhua.utils.QuickClick;
import carton.fmy.com.yuanmanhua.utils.SnackbarUtil;
import mehdi.sakout.fancybuttons.FancyButton;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class IntroduceActivity extends BaseSwipeActivity {
    //书籍id
    String bookId;
    //展示图
    private ImageView iv_show;
    //作者
    private TextView tv_author;
    //排名
    private TextView tv_ranking;
    //书名
    private TextView tv_show;
    //简介
    private ExpandableTextView expand_text_view;
    //分类
    private TextView tv_classify2;
    //书籍信息
    CatalogueBean catalogueBean;

    // 用于显示目录
    private RecyclerView recycler_view;
    //显示目录的布局管理器
    private LinearLayoutManager linearLayoutManager;
    //目录适配器
    private CataloguAdapter cataloguAdapter;
    //书籍目录信息
    List<CatalogueBean.ChapterBean> chapterListBean;
    //正在加载的dialog
    private Dialog dialog;

    private Snackbar imgSnackbar;
    //收藏按钮
    private FancyButton btn_collect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalogue);
        //初始化view
        initView();
        //点击初始化
        initOnClick();
        //初始化数据
        initData();
        //初始化网络下载
        initNet();

    }

    private void initOnClick() {
        btn_collect.setOnClickListener(view -> {
            //防止用户连续点击
            if (!QuickClick.quickClick()){
                CharSequence text = btn_collect.getText();
                if ("收藏".equals(text)){
                    CollectUtilt.putCollect(IntroduceActivity.this,catalogueBean.getId());
                    btn_collect.setText("已收藏");
                }else{
                    btn_collect.setText("收藏");
                    CollectUtilt.removeCollect(IntroduceActivity.this,catalogueBean.getId());
                }
            }

        });
    }

    private void initData() {
        Intent intent = getIntent();
        bookId = intent.getStringExtra("bookId");

        if (CollectUtilt.isCollect(this,bookId)){
            btn_collect.setText("已收藏");
        }
    }

    private void initView() {
        //展示图
        iv_show = ((ImageView) findViewById(R.id.iv_show));
        //作者
        tv_author = (TextView) findViewById(R.id.tv_author);
        //排名
        tv_ranking = ((TextView) findViewById(R.id.tv_ranking2));
        //书名
        tv_show = ((TextView) findViewById(R.id.tv_show));
        //收藏按钮
        btn_collect = ((FancyButton) findViewById(R.id.btn_collect));
        //简介
        expand_text_view = ((ExpandableTextView) findViewById(R.id.expand_text_view));
        //分类
        tv_classify2 = ((TextView) findViewById(R.id.tv_classify2));
        //获取RecycalView 用于显示目录
        recycler_view = ((RecyclerView) findViewById(R.id.recycler_view));
        //显示目录的布局管理器
        linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        //设置布局
        recycler_view.setLayoutManager(linearLayoutManager);
        //设置数据
        cataloguAdapter = new CataloguAdapter(chapterListBean);
        //设置适配器
        recycler_view.setAdapter(cataloguAdapter);
        //每个目录的点击事件
        recycler_view.addOnItemTouchListener(onItemChildClickListener);




        //正在加载的dialog
        dialog = DialogUtil.getDialog(this);
        dialog.show();
        //当dialog 正在显示的时候证明数据没有加载完成 那么用户按下返回键那么直接finish
        dialog.setOnKeyListener((dialogInterface, i, keyEvent) -> {
            if (i== KeyEvent.KEYCODE_BACK) {
              finish();
                return  true;
            }
            return  false;
        });



    }


    //每个目录的点击事件
    OnItemChildClickListener onItemChildClickListener = new OnItemChildClickListener() {
        @Override
        public void onSimpleItemChildClick(BaseQuickAdapter baseQuickAdapter, View view, int i) {

            //获得书籍id
            String bookId = catalogueBean.getId();
            //获取本书籍章节
            String number = catalogueBean.getChapter().get(i).getNumber();
            //跳转意图
            Intent intent = new Intent(IntroduceActivity.this, ShowActivity.class);
            //放入书籍
            intent.putExtra("bookId", bookId);
            //章节
            intent.putExtra("number", number);
            //传入第几话
            intent.putExtra("title", catalogueBean.getChapter().get(i).getTitle());

            startActivity(intent);
        }
    };

    /**
     * 初始化网络
     */
    public void initNet() {

        UrlCatalogueInterface urlCatalogueInterface = NetUtil.getRetrofit().create(UrlCatalogueInterface.class);
        Call<CatalogueBean> catalogueBeanClass = urlCatalogueInterface.get(bookId);
        catalogueBeanClass.enqueue(new Callback<CatalogueBean>() {
            @Override
            public void onResponse(Call<CatalogueBean> call, Response<CatalogueBean> response) {
                catalogueBean = response.body();
                if (catalogueBean != null) {
                    //设置分类
                    tv_classify2.setText(catalogueBean.getTheme());
                    //设置书名
                    tv_show.setText(catalogueBean.getName());
                    //设置作者
                    tv_author.setText("作者:" + catalogueBean.getAuthor());
                    //设置排名
                    tv_ranking.setText(catalogueBean.getRanking());
                    //设置简介
                    expand_text_view.setText(catalogueBean.getIntroduction());
                    //下载展示图
                    Glide.with(IntroduceActivity.this).load(catalogueBean.getIcon()).crossFade().placeholder(R.mipmap.placeholder_item).into(iv_show);
                    //获取目录
                    chapterListBean = catalogueBean.getChapter();
                    //设置数据
                    cataloguAdapter.setNewData(chapterListBean);
                    //刷新
                    cataloguAdapter.notifyDataSetChanged();
                    //关闭正在记载的动画
                    if (dialog!=null&&dialog.isShowing()){
                        dialog.dismiss();
                    }
                    if (imgSnackbar!=null&&imgSnackbar.isShown()){
                        imgSnackbar.dismiss();
                    }
                }

            }

            @Override
            public void onFailure(Call<CatalogueBean> call, Throwable t) {
                imgSnackbar = SnackbarUtil.getImgSnackbar(tv_show, "下载错误,返回重试看看", Snackbar.LENGTH_INDEFINITE, IntroduceActivity.this, -1);
                imgSnackbar.show();
            }
        });
    }
}
