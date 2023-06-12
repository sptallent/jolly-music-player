package sage.musicplayer.ListAdapter;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.PagerAdapter;

import java.util.ArrayList;
import java.util.List;

public class EditViewPagerAdapter extends PagerAdapter {

    private final List<View> mViewList = new ArrayList<>();
    private final List<String> mTitleList = new ArrayList<>();

    public void addView(View view, String title) {
        mViewList.add(view);
        mTitleList.add(title);
    }

    @Override
    public int getCount() {
        return mViewList.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        container.addView(mViewList.get(position));
        return mViewList.get(position);
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView(mViewList.get(position));
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return mTitleList.get(position);
    }
}
