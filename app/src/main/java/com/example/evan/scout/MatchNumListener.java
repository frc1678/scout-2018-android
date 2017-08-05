package com.example.evan.scout;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * Created by Calvin on 7/26/17.
 */

public class MatchNumListener {
    public static Integer currentMatchNumber;

    public MatchNumListener(MatchFirebaseInterface mfi) {
        setupMatchNumListening(mfi);
    }

    public void setupMatchNumListening(final MatchFirebaseInterface mfi) {
        ValueEventListener matchListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    currentMatchNumber = dataSnapshot.getValue(Integer.class);
                } else {
                    currentMatchNumber = -1;
                }
                mfi.onMatchChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        FirebaseDatabase.getInstance().getReference().child("currentMatchNum").addValueEventListener(matchListener);
    }

    public interface MatchFirebaseInterface {

        void onMatchChanged();
    }
}
