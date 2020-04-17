package com.example.hangout;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class tabaccessoradapter extends FragmentPagerAdapter
{


    public tabaccessoradapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {

        switch(position){
            case 0:
                ChatsFragment chatsFragment= new ChatsFragment();
                return chatsFragment;
            case 1:
                GroupsFragment groupsFragment= new GroupsFragment();
                return groupsFragment;
            case 2:
                ContextFragment contextFragment= new ContextFragment();
                return contextFragment;
            case 3:
                RequestsFragment requestfragment= new RequestsFragment();
                return requestfragment;

            default:
                return null;

        }


    }

    @Override
    public int getCount() {
        return 4;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {

        switch(position){
            case 0:
                return "Chats";
            case 1:
                return "Groups";
            case 2:
                return "Friends";
            case 3:
                return "Requests";
            default:
                return null;

        }


    }
}
