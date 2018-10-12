package com.bilibili.following.prvlibrary;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.ArrayMap;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.ViewGroup;

import com.bilibili.following.prvlibrary.binder.Binder;
import com.bilibili.following.prvlibrary.binder.BinderResult;
import com.bilibili.following.prvlibrary.binder.ItemBinder;
import com.bilibili.following.prvlibrary.viewholder.ViewHolder;

import java.util.ArrayList;
import java.util.List;

public abstract class BasePrvAdapter<T> extends RecyclerView.Adapter<ViewHolder> {

    @NonNull
    private final List<T> mItems = new ArrayList<>();
    @NonNull
    private SparseArray<List<Binder<? extends T, ? extends ViewHolder>>> mBinderListCache;
    @NonNull
    private List<Integer> mViewHolderToItemPositionCache;
    @NonNull
    private List<Integer> mItemPositionToFirstViewHolderPositionCache;
    @NonNull
    private ArrayMap<Class<? extends T>, ItemBinder<? extends T, ? extends Binder>> mItemBinderMap;
    @NonNull
    protected SparseArray<Binder<? extends T, ? extends ViewHolder>> mBinderInfo;

    protected BasePrvAdapter() {
        mBinderListCache = new SparseArray<>();
        mViewHolderToItemPositionCache = new ArrayList<>();
        mItemPositionToFirstViewHolderPositionCache = new ArrayList<>();
        mItemBinderMap = new ArrayMap<>();
        mBinderInfo = new SparseArray<>();
    }

    @Override
    public int getItemCount() {
        return mViewHolderToItemPositionCache.size();
    }

    @Override
    public int getItemViewType(final int position) {
        final BinderResult<? extends T> result = computeItemAndBinderIndex(position);

        final Binder binder = result.getBinder();
        final int viewType;

        if (binder != null) {
            viewType = binder.getViewType();
        } else {
            viewType = -1;
        }

        return viewType;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, final int viewType) {
        return mBinderInfo.get(viewType).create(parent);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int viewHolderPosition) {
        final BinderResult<? extends T> result = computeItemAndBinderIndex(viewHolderPosition);
        final Binder binder = result.getBinder();

        if (binder != null && result.item != null && result.binderList != null) {
            binder.bind(result.item, holder, result.binderList, result.binderIndex);
        }
    }

    protected void register(@NonNull final Class<? extends T> modelType,
                            @NonNull final ItemBinder<? extends T, ? extends Binder> parts) {
        mItemBinderMap.put(modelType, parts);
    }

    public void add(@NonNull final T item) {
        add(mItems.size(), item);
    }

    public void add(final int position, @NonNull final T item) {
        final List<Binder<? extends T, ? extends ViewHolder>> binders = getParts(item, position);
        if (binders == null || binders.isEmpty()) {
            return;
        }

        final int numViewHolders = getViewHolderCount(position);

        mItems.add(position, item);
        mBinderListCache.put(position, binders);

        notifyItemRangeInserted(numViewHolders, binders.size());

        final List<Integer> itemPositions = new ArrayList<>();
        for (int i = 0; i < binders.size(); i++) {
            itemPositions.add(position);
        }

        mViewHolderToItemPositionCache.addAll(numViewHolders, itemPositions);
        for (int viewHolderIndex = numViewHolders + binders.size(); viewHolderIndex < mViewHolderToItemPositionCache.size();
             viewHolderIndex++) {
            mViewHolderToItemPositionCache.set(viewHolderIndex, mViewHolderToItemPositionCache.get(viewHolderIndex) + 1);
        }

        mItemPositionToFirstViewHolderPositionCache.add(position, numViewHolders);
        for (int itemIndex = position + 1; itemIndex < mItemPositionToFirstViewHolderPositionCache.size(); itemIndex++) {
            mItemPositionToFirstViewHolderPositionCache.set(itemIndex,
                    mItemPositionToFirstViewHolderPositionCache.get(itemIndex) + binders.size());
        }
    }

    private BinderResult<? extends T> computeItemAndBinderIndex(final int viewHolderPosition) {
        final int itemIndex = mViewHolderToItemPositionCache.get(viewHolderPosition);
        final T item = mItems.get(itemIndex);
        final List binders = mBinderListCache.get(itemIndex);

        final int firstVHPosForItem = mItemPositionToFirstViewHolderPositionCache.get(itemIndex);

        return new BinderResult<>(item, itemIndex, binders, viewHolderPosition - firstVHPosForItem);
    }

    private int getViewHolderCount(final int itemPosition) {
        if (itemPosition >= 0 && mItemPositionToFirstViewHolderPositionCache.size() > 0) {
            if (itemPosition >= mItemPositionToFirstViewHolderPositionCache.size()) {
                return mViewHolderToItemPositionCache.size();
            } else {
                return mItemPositionToFirstViewHolderPositionCache.get(itemPosition);
            }
        } else {
            return 0;
        }
    }

    @Nullable
    private List<Binder<? extends T, ? extends ViewHolder>> getParts(final T model, final int position) {
        final List<Binder<? extends T, ? extends ViewHolder>> list;

        final ItemBinder itemBinder = mItemBinderMap.get(model.getClass());

        if (itemBinder != null) {
            list = itemBinder.getBinderList(model, position);
        } else {
            list = null;
        }

        return list;
    }

}
