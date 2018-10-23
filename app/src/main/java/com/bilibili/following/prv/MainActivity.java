package com.bilibili.following.prv;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.bilibili.following.prv.model.ColorNamePrimitive;

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

		List<ColorNamePrimitive> list = new ArrayList<>();
		list.add(new ColorNamePrimitive(R.color.red_base_variant_0, "dark red"));
		list.add(new ColorNamePrimitive(R.color.red_base_variant_1, "red"));
		list.add(new ColorNamePrimitive(R.color.red_base_variant_2, "bright red"));
		list.add(new ColorNamePrimitive(R.color.red_base_variant_3, "shy red"));
		list.add(new ColorNamePrimitive(R.color.red_base_variant_4, "embarrassed red"));


		mPrimitiveAdapter.addAll(list);

		recyclerView.setAdapter(mPrimitiveAdapter);
	}
}
