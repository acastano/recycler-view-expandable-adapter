package com.andrescastano.ui.common;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public abstract class RecyclerViewExpandableAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public void resetExpanded() {
        expandedPositions.clear();
    }

    public boolean isParentExpanded(int position) {
        Long id = expandedPositions.get(position);
        return id != null && id == getGroupId(position);
    }

    public void collapseParent(int position) {
        expandedPositions.remove(position);
    }

    public void expandParent(int position, long id) {
        expandedPositions.put(position, id);
    }

    private class Parent {
        private final int parentPosition;
        private final boolean isExpanded;

        Parent(int parentPosition, boolean isExpanded) {
            this.isExpanded = isExpanded;
            this.parentPosition = parentPosition;
        }
    }

    private class Child {
        private final int parentPosition;
        private final int childPosition;

        Child(int parentPosition, int childPosition) {
            this.parentPosition = parentPosition;
            this.childPosition = childPosition;
        }
    }

    private SparseArray<Child> childPositions = new SparseArray<>();
    private SparseArray<Parent> parentPositions = new SparseArray<>();
    private SparseArray<Long> expandedPositions = new SparseArray<>();

    public abstract int getLayoutId(int viewType);
    public abstract RecyclerView.ViewHolder getViewHolder(View view, int viewType);

    public abstract int getGroupCount();
    public abstract long getGroupId(int i);
    public abstract int getGroupType(int groupPosition);
    public abstract void onBindGroupViewHolder(int position, int groupPosition, boolean isExpanded, RecyclerView.ViewHolder holder);

    public abstract int getChildrenCount(int i);
    public abstract long getChildId(int i, int j);
    public abstract int getChildType(int groupPosition, int childPosition);
    public abstract void onBindChildViewHolder(final int groupPosition, final int childPosition, RecyclerView.ViewHolder holder);

    @Override
    public long getItemId(int position) {
        Parent parent = parentPositions.get(position);
        Child children = childPositions.get(position);
        if (children != null) {
            return getChildId(children.parentPosition, children.childPosition);
        } else if (parent != null) {
            return getGroupId(parent.parentPosition);
        }
        return super.getItemId(position);
    }

    @Override
    public int getItemCount() {
        childPositions.clear();
        parentPositions.clear();
        int totalChildCount = 0;
        int groupCount = getGroupCount();

        for (int i = 0; i < groupCount; i++) {
            int position = i + totalChildCount;

            Parent parent = new Parent(i, isParentExpanded(i));
            parentPositions.put(position, parent);

            int count = getChildrenCount(i);
            if (count > 0 && parent.isExpanded) {
                for (int j = 0; j < count; j++) {
                    Child child = new Child(i, j);
                    int childPosition = position + j + 1;
                    childPositions.put(childPosition, child);
                }
                totalChildCount += count;
            }
        }
        return groupCount + totalChildCount;
    }

    @Override
    public int getItemViewType(int position) {
        Parent parent = parentPositions.get(position);
        Child children = childPositions.get(position);
        if (children != null) {
            return getChildType(children.parentPosition, children.childPosition);
        } else if (parent != null) {
            return getGroupType(parent.parentPosition);
        }
        return super.getItemViewType(position);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(getLayoutId(viewType), parent, false);
        RecyclerView.ViewHolder viewHolder = getViewHolder(view, viewType);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Parent parent = parentPositions.get(position);
        Child children = childPositions.get(position);
        if (children != null) {
            onBindChildViewHolder(children.parentPosition, children.childPosition, holder);
        } else if (parent != null) {
            onBindGroupViewHolder(position, parent.parentPosition, parent.isExpanded, holder);
        }
    }
}
