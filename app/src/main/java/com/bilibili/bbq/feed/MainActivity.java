package com.bilibili.bbq.feed;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.bilibili.bbq.feed.model.TestPrimitive;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

	PrimitiveAdapter mPrimitiveAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);


		mPrimitiveAdapter = new PrimitiveAdapter();
		final RecyclerView recyclerView = findViewById(R.id.list);

		recyclerView.setLayoutManager(new LinearLayoutManager(this));

		List<TestPrimitive> list = new ArrayList<>();
		list.add(new TestPrimitive(R.color.colorPrimary, "colorPrimary"));
		list.add(new TestPrimitive(R.color.colorPrimaryDark, "colorPrimaryDark"));


		mPrimitiveAdapter.addAll(list);

		recyclerView.setAdapter(mPrimitiveAdapter);
	}
}
