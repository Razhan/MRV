package com.bilibili.bbq.feedlib;

import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.ArrayMap;
import android.support.v4.util.Pair;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.ViewGroup;

import com.bilibili.bbq.feedlib.binder.Binder;
import com.bilibili.bbq.feedlib.binder.BinderResult;
import com.bilibili.bbq.feedlib.binder.ItemBinder;
import com.bilibili.bbq.feedlib.viewholder.ViewHolder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

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
    private SparseArray<Binder<? extends T, ? extends ViewHolder>> mBinderList;

    protected BasePrvAdapter() {
        mBinderListCache = new SparseArray<>();
        mViewHolderToItemPositionCache = new ArrayList<>();
        mItemPositionToFirstViewHolderPositionCache = new ArrayList<>();
        mItemBinderMap = new ArrayMap<>();
        mBinderList = new SparseArray<>();
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
        return mBinderList.get(viewType).create(parent);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int viewHolderPosition) {
        this.onBindViewHolder(holder, viewHolderPosition, Collections.emptyList());
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int viewHolderPosition, @NonNull List<Object> payloads) {
        final BinderResult<? extends T> result = computeItemAndBinderIndex(viewHolderPosition);
        final Binder binder = result.getBinder();

        if (binder != null && result.item != null && result.binderList != null) {
            binder.bind(result.item, holder, result.binderList, result.binderIndex, payloads);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onViewRecycled(@NonNull ViewHolder holder) {
        if (holder.getAdapterPosition() < 0) {
            return;
        }

        final BinderResult<? extends T> result = computeItemAndBinderIndex(holder.getAdapterPosition());
        final Binder binder = result.getBinder();

        if (binder != null && holder.getItemViewType() == binder.getViewType()) {
            binder.unbind(holder);
        }
    }

    @Override
    public void onViewAttachedToWindow(@NonNull ViewHolder holder) {
        if (holder.getAdapterPosition() < 0) {
            return;
        }

        final BinderResult<? extends T> result = computeItemAndBinderIndex(holder.getAdapterPosition());
        final Binder binder = result.getBinder();

        if (binder != null && holder.getItemViewType() == binder.getViewType()) {
            binder.onViewAttachedToWindow(holder);
        }

    }

    @Override
    public void onViewDetachedFromWindow(@NonNull ViewHolder holder) {
        if (holder.getAdapterPosition() < 0) {
            return;
        }

        final BinderResult<? extends T> result = computeItemAndBinderIndex(holder.getAdapterPosition());
        final Binder binder = result.getBinder();

        if (binder != null && holder.getItemViewType() == binder.getViewType()) {
            binder.onViewDetachedFromWindow(holder);
        }

    }

    protected void registerItemBinder(@NonNull final Class<? extends T> modelType,
                                      @NonNull final ItemBinder<? extends T, ? extends Binder> parts) {
        mItemBinderMap.put(modelType, parts);
    }

    protected void registerBinder(@LayoutRes int type, Binder<? extends T, ? extends ViewHolder> binder) {
        mBinderList.put(type, binder);
    }

    public void add(@NonNull final T item) {
        add(mItems.size(), item);
    }

    public void addAll(@NonNull final List<? extends T> items) {
        addAll(mItems.size(), items);
    }

    public void addAll(final int position, @NonNull final List<? extends T> items) {
        if (items == null || items.isEmpty()) {
            return;
        }

        for (int i = 0; i < items.size(); i++) {
            add(position + i, items.get(i));
        }
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

        // TODO: 10/19/18 O(n)操作 考虑优化
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

    public T remove(final int itemPosition) {
        T item;

        if (isItemPositionWithinBounds(itemPosition)) {
            final int numViewHolders = getViewHolderCount(itemPosition);
            item = mItems.get(itemPosition);

            final List<Binder<? extends T, ? extends ViewHolder>> binders = mBinderListCache.get(itemPosition);

            mItems.remove(itemPosition);

            // TODO: 10/19/18 O(n)操作 考虑优化
            for (final ListIterator<Integer> iterator = mViewHolderToItemPositionCache.listIterator(); iterator.hasNext(); ) {
                if (iterator.next() == itemPosition) {
                    iterator.remove();
                }
            }

            for (int viewHolderIndex = numViewHolders; viewHolderIndex < mViewHolderToItemPositionCache.size(); viewHolderIndex++) {
                mViewHolderToItemPositionCache.set(viewHolderIndex, mViewHolderToItemPositionCache.get(viewHolderIndex) - 1);
            }

            mItemPositionToFirstViewHolderPositionCache.remove(itemPosition);

            if (binders != null) {
                for (int itemIndex = itemPosition; itemIndex < mItemPositionToFirstViewHolderPositionCache.size(); itemIndex++) {
                    mItemPositionToFirstViewHolderPositionCache.set(itemIndex,
                            mItemPositionToFirstViewHolderPositionCache.get(itemIndex) - binders.size());
                }
            }

            mBinderListCache.remove(itemPosition);

            if (binders != null) {
                notifyItemRangeRemoved(numViewHolders, binders.size());
            }
        } else {
            item = null;
        }

        return item;
    }

    public void set(@NonNull final List<? extends T> items) {
        if (items == null || items.isEmpty()) {
            return;
        }

        clear();
        notifyDataSetChanged();

        addAll(items);
    }

    public void clear() {
        mItems.clear();
        mBinderListCache.clear();
        mViewHolderToItemPositionCache.clear();
        mItemPositionToFirstViewHolderPositionCache.clear();
    }

    @Nullable
    public List<Binder<? extends T, ? extends ViewHolder>> getBindersForPosition(final int itemPosition) {
        final List<Binder<? extends T, ? extends ViewHolder>> binders;

        if (isItemPositionWithinBounds(itemPosition)) {
            binders = mBinderListCache.get(itemPosition);
        } else {
            binders = null;
        }

        return binders;
    }

    /**
     *  返回结果first是offset，second是count
     */
    @Nullable
    public Pair<Integer, Integer> getViewHolderRange(final int itemPosition) {
        final Pair<Integer, Integer> range;

        if (isItemPositionWithinBounds(itemPosition)) {
            final int numViewHolders = getViewHolderCount(itemPosition);

            final List<Binder<? extends T, ? extends ViewHolder>> binders = mBinderListCache.get(itemPosition);

            range = new Pair<>(numViewHolders, binders.size());
        } else {
            range = null;
        }

        return range;
    }

    public int getItemPosition(final int viewHolderPosition) {
        if (isViewHolderPositionWithinBounds(viewHolderPosition)) {
            return mViewHolderToItemPositionCache.get(viewHolderPosition);
        } else {
            return -1;
        }
    }

    public T getItem(final int viewHolderPosition) {
        int index = getItemPosition(viewHolderPosition);
        if (index >= 0 && index <= mItems.size() - 1) {
            return mItems.get(getItemPosition(viewHolderPosition));
        } else {
            return null;
        }
    }

    @NonNull
    public List<T> getItems() {
        return mItems;
    }

    private boolean isItemPositionWithinBounds(final int itemPosition) {
        return itemPosition >= 0 && itemPosition < mItems.size();
    }

    private boolean isViewHolderPositionWithinBounds(final int viewHolderPosition) {
        return viewHolderPosition >= 0 && viewHolderPosition < mViewHolderToItemPositionCache.size();
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

    @SuppressWarnings("unchecked")
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
