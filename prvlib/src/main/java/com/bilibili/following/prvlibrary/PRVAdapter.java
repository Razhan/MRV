package com.bilibili.following.prvlibrary;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.ArrayMap;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class PRVAdapter<T> extends RecyclerView.Adapter<ViewHolder> {

    @NonNull
    private final List<T> mItems = new ArrayList<>();
    //    @NonNull
//    private final Map<MT, ActionListener<? extends T, VH, ? extends VH>> mActionListenerMap;
    @NonNull
    private SparseArray<List<Binder<? super T, ? extends ViewHolder>>> mBinderListCache;
    @NonNull
    private SparseIntArray mViewHolderToItemPositionCache;
    @NonNull
    private SparseIntArray mItemPositionToFirstViewHolderPositionCache;
    @NonNull
    protected ArrayMap<Class, ItemBinder<? extends T, ? extends Binder<? extends T, ? extends ViewHolder>>> mItemBinderMap;

    @NonNull
//    private final Map<Integer, Class<? extends VH>> mViewTypeToViewHolderClassMap;
//    @NonNull
//    private final Map<MT, ItemBinder<? extends T, ? extends VH, ? extends Binder>> mItemBinderMap;
//    @NonNull
    private SparseArray<? extends Binder> mBinderInfo;

    public PRVAdapter() {
//        mActionListenerMap = new ArrayMap<>();
//        mBinderListCache = new ArrayList<>();
//        mViewHolderToItemPositionCache = new ArrayList<>();
//        mItemPositionToFirstViewHolderPositionCache = new ArrayList<>();
//        mViewTypeToViewHolderClassMap = new ArrayMap<>();
//        mItemBinderMap = new ArrayMap<>();
    }

    @Override
    public int getItemCount() {
        return mViewHolderToItemPositionCache.size();
    }

    @Override
    public int getItemViewType(final int position) {
        final BinderResult<? super T> result = computeItemAndBinderIndex(position);

        final Binder<? super T, ? extends ViewHolder> binder = result.getBinder();
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
    @SuppressWarnings("unchecked")
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int viewHolderPosition) {
        final BinderResult<? super T> result = computeItemAndBinderIndex(viewHolderPosition);
        final Binder binder = result.getBinder();

        if (binder != null && result.item != null && result.binderList != null) {
            binder.bind(result.item, holder, result.binderList, result.binderIndex);
        }
    }

    public void add(@NonNull final T item) {
        add(mItems.size(), item);
    }

    public void add(final int position, @NonNull final T item) {
        final int numViewHolders = getViewHolderCount(position);
        final List<Binder<? super T, ? extends ViewHolder>> binders = getParts(item, position);


    }


    private BinderResult<? super T> computeItemAndBinderIndex(final int viewHolderPosition) {
        final int itemIndex = mViewHolderToItemPositionCache.get(viewHolderPosition);
        final T item = mItems.get(itemIndex);
        final List<Binder<? super T, ? extends ViewHolder>> binders = mBinderListCache.get(itemIndex);

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
    private List<Binder<? super T, ? extends ViewHolder>> getParts(final T model, final int position) {
        final List<Binder<? super T, ? extends ViewHolder>> list;

        final ItemBinder itemBinder = mItemBinderMap.get(model.getClass());

        if (itemBinder != null) {
            list = itemBinder.getBinderList(model, position);
        } else {
            list = null;
        }

        return list;
    }

}
