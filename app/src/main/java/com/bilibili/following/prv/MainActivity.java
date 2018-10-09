package com.bilibili.following.prv;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.bilibili.following.prv.model.ColorNamePrimitive;

public class MainActivity extends AppCompatActivity {

	PrimitiveAdapter mPrimitiveAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);


		mPrimitiveAdapter = new PrimitiveAdapter();
		final RecyclerView recyclerView = findViewById(R.id.list);

		recyclerView.setLayoutManager(new LinearLayoutManager(this));

		mPrimitiveAdapter.add(new ColorNamePrimitive(R.color.red_base_variant_0, "dark red"));
		mPrimitiveAdapter.add(new ColorNamePrimitive(R.color.red_base_variant_1, "red"));
		mPrimitiveAdapter.add(new ColorNamePrimitive(R.color.red_base_variant_2, "bright red"));
		mPrimitiveAdapter.add(new ColorNamePrimitive(R.color.red_base_variant_3, "shy red"));
		mPrimitiveAdapter.add(new ColorNamePrimitive(R.color.red_base_variant_4, "embarrassed red"));

		recyclerView.setAdapter(mPrimitiveAdapter);
	}
}
