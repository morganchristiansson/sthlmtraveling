/*
 	Copyright (c) 2008-2009 CommonsWare, LLC
	portions Copyright (c) 2008 Jeffrey Sharkey
	portions Copyright (c) 2009 Johan Nilsson

	This program is free software: you can redistribute it and/or modify
	it under the terms of the GNU General Public License as published by
	the Free Software Foundation, either version 3 of the License, or
	(at your option) any later version.

	This program is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.	See the
	GNU General Public License for more details.

	You should have received a copy of the GNU General Public License
	along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.markupartist.sthlmtraveling;

import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.BaseAdapter;
import java.util.ArrayList;
import java.util.List;

/**
 * Sectioned adapter groups several adapters into one.
 */
abstract public class SectionedAdapter extends BaseAdapter {
    abstract protected View getHeaderView(Section section, int index,
            View convertView, ViewGroup parent);

    private List<Section> sections = new ArrayList<Section>();
    private static int TYPE_SECTION_HEADER = 0;

    public SectionedAdapter() {
        super();
    }

    public void addSection(int id, String caption, Adapter adapter) {
        sections.add(new Section(id, caption, adapter));
    }

    /**
     * Get current section and the given position.
     * @author johan
     * @param position the position
     * @return the current section
     */
    public Section getSection(int position) {
        for (Section section : this.sections) {
            if (position == 0) {
                return (section);
            }

            int size = section.adapter.getCount() + 1;

            if (position < size) {
                return section;
            }

            position -= size;
        }

        return null;
    }

    /**
     * Get the section id of the inner adapter
     * @author johan
     * @param position the position
     * @return the inner adapters position
     */
    public int getSectionIndex(int position) {
        int sectionIndex = 0;

        for (Section section : this.sections) {
            int size = section.adapter.getCount() + 1;

            if (position < size) {
                return position - 1;
            }

            position -= size;
            sectionIndex++;
        }

        return sectionIndex;
    }
    
    public Object getItem(int position) {
        for (Section section : this.sections) {
            if (position == 0) {
                return (section);
            }

            int size = section.adapter.getCount() + 1;

            if (position < size) {
                return (section.adapter.getItem(position - 1));
            }

            position -= size;
        }

        return (null);
    }

    public int getCount() {
        int total = 0;

        for (Section section : this.sections) {
            total += section.adapter.getCount() + 1; // add one for header
        }

        return (total);
    }

    public int getViewTypeCount() {
        int total = 1; // one for the header, plus those from sections

        for (Section section : this.sections) {
            total += section.adapter.getViewTypeCount();
        }

        return (total);
    }

    public int getItemViewType(int position) {
        int typeOffset = TYPE_SECTION_HEADER + 1; // start counting from here

        for (Section section : this.sections) {
            if (position == 0) {
                return (TYPE_SECTION_HEADER);
            }

            int size = section.adapter.getCount() + 1;

            if (position < size) {
                return (typeOffset + section.adapter
                        .getItemViewType(position - 1));
            }

            position -= size;
            typeOffset += section.adapter.getViewTypeCount();
        }

        return (-1);
    }

    public boolean areAllItemsSelectable() {
        return (false);
    }

    public boolean isEnabled(int position) {
        return (getItemViewType(position) != TYPE_SECTION_HEADER);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        int sectionIndex = 0;

        for (Section section : this.sections) {
            if (position == 0) {
                return (getHeaderView(section, sectionIndex,
                        convertView, parent));
            }

            int size = section.adapter.getCount() + 1;

            if (position < size) {
                return (section.adapter.getView(position - 1, convertView,
                        parent));
            }

            position -= size;
            sectionIndex++;
        }

        return (null);
    }

    @Override
    public long getItemId(int position) {
        return (position);
    }

    class Section {
        int id;
        String caption;
        Adapter adapter;

        Section(int id, String caption, Adapter adapter) {
            this.id = id;
            this.caption = caption;
            this.adapter = adapter;
        }
    }
}