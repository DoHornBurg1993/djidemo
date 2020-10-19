package com.ast.djisdk;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.amap.api.maps.AMap;
import com.amap.api.maps.AMap.OnMapClickListener;
import com.amap.api.maps.CameraUpdate;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.CoordinateConverter;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CircleOptions;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.Polygon;
import com.amap.api.maps.model.PolygonOptions;
import com.amap.api.maps.model.Polyline;
import com.amap.api.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;

import dji.common.flightcontroller.flyzone.FlyZoneCategory;
import dji.common.flightcontroller.flyzone.FlyZoneInformation;
import dji.common.flightcontroller.flyzone.SubFlyZoneInformation;
import dji.common.flightcontroller.flyzone.SubFlyZoneShape;
import dji.common.model.LocationCoordinate2D;
import dji.log.DJILog;

/**
 * @author： DuHongBo
 */
@SuppressLint("ValidFragment")
public class GaoDeMapFragment extends Fragment implements OnMapClickListener{
    private AMap aMap;
    private Boolean is_marker=false;
    public Boolean is_big=false;
    private Marker droneMarker = null, droneMarker_home = null;
    private List<Marker> arrayList_xunxian_Marker = new ArrayList<>();
    private static Context context;
    private clickLatLngListener latLngListener;
    private MapView mapView;
    private View mapLayout;
    private static GaoDeMapFragment fragment=null;
    private FlyfrbBasePainter painter = new FlyfrbBasePainter();
    private ArrayList<Integer> unlockableIds = new ArrayList<Integer>();

    @Override
    public void setArguments(Bundle args) {
        super.setArguments(args);
    }
    public static Fragment newInstance(){
        if(fragment==null){
            synchronized(GaoDeMapFragment.class){
                if(fragment==null){
                    fragment=new GaoDeMapFragment();
                }
            }
        }
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (mapLayout == null) {
            mapLayout = inflater.inflate(R.layout.fmaplayout, container, false);
            mapView = (MapView) mapLayout.findViewById(R.id.mapView);
            mapView.onCreate(savedInstanceState);
            aMap = mapView.getMap();
            aMap.setOnMapClickListener(this);
        }else {
            if (mapLayout.getParent() != null) {
                ((ViewGroup) mapLayout.getParent()).removeView(mapLayout);
            }
        }
        return mapLayout;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onResume() {
        Log.i("sys", "mf onResume");
        super.onResume();
        mapView.onResume();
    }

    /**
     * 方法必须重写
     * map的生命周期方法
     */
    @Override
    public void onPause() {
        Log.i("sys", "mf onPause");
        super.onPause();
        mapView.onPause();
    }

    /**
     * 方法必须重写
     * map的生命周期方法
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        Log.i("sys", "mf onSaveInstanceState");
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    /**
     * 方法必须重写
     * map的生命周期方法
     */
    @Override
    public void onDestroy() {
        Log.i("sys", "mf onDestroy");
        super.onDestroy();
        mapView.onDestroy();
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        latLngListener=(clickLatLngListener)activity;
    }

    @Override
    public void onMapClick(LatLng latLng) {
        if(!is_big)
            latLngListener.clicklatlnglistener(latLng, 88);
        else if (is_marker)
            markWaypoint(latLng);
    }

    public interface clickLatLngListener {
        void clicklatlnglistener(LatLng v, int i);
    }

    public static boolean checkGpsCoordination(double latitude, double longitude) {
        return (latitude > -90 && latitude < 90 && longitude > -180 && longitude < 180) && (latitude != 0f && longitude != 0f);
    }

    public void fly_LatLng(final LatLng latLng, final float hd) {
        CoordinateConverter converter = new CoordinateConverter(context);
        converter.from(CoordinateConverter.CoordType.GPS);
        converter.coord(latLng);
// 执行转换操作
        LatLng desLatLng = converter.convert();
        final MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(desLatLng);
        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.aircraft));
        if (droneMarker != null) {
            droneMarker.remove();
        }
        if (checkGpsCoordination(latLng.latitude, latLng.longitude)) {
            droneMarker = aMap.addMarker(markerOptions.anchor(0.5f, 0.5f));
            droneMarker.setRotateAngle(-hd+aMap.getCameraPosition().bearing);
        }
    }

    public void home_LatLng(final LatLng latLng) {
        CoordinateConverter converter = new CoordinateConverter(context);
        converter.from(CoordinateConverter.CoordType.GPS);
        converter.coord(latLng);
// 执行转换操作
        LatLng desLatLng = converter.convert();
        final MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(desLatLng);
        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.home));
        if (droneMarker_home != null) {
            droneMarker_home.remove();
        }
        if (checkGpsCoordination(latLng.latitude, latLng.longitude)) {
            droneMarker_home = aMap.addMarker(markerOptions.anchor(0.5f, 0.5f));
        }
    }

    public void cameraUpdate(final LatLng latLng) {
        CoordinateConverter converter = new CoordinateConverter(context);
        converter.from(CoordinateConverter.CoordType.GPS);
        converter.coord(latLng);
// 执行转换操作
        LatLng desLatLng = converter.convert();
        float zoomlevel = 18.0f;//缩放级别
        CameraUpdate cu = CameraUpdateFactory.newLatLngZoom(desLatLng, zoomlevel);
        aMap.moveCamera(cu);
    }

    public void markWaypoint_xunxian(boolean is_marker) {
        //Create MarkerOptions object
        this.is_marker = is_marker;
    }

    public void map_clear() {
        aMap.clear();
        arrayList_xunxian_Marker.clear();
    }

    private void markWaypoint(LatLng point) {
        //Create MarkerOptions object
        Marker marker = null;
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(point);
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));


        markerOptions.draggable(true);
        marker = aMap.addMarker(markerOptions);
        arrayList_xunxian_Marker.add(marker);
        int j = arrayList_xunxian_Marker.size();
        if (j >= 2) {
            Polyline p = aMap.addPolyline((new PolylineOptions()).add(arrayList_xunxian_Marker.get(j - 2).getPosition(), arrayList_xunxian_Marker.get(j - 1).getPosition()).color(
                    Color.RED));
            LatLng point_m = new LatLng((arrayList_xunxian_Marker.get(j - 2).getPosition().latitude + arrayList_xunxian_Marker.get(j - 1).getPosition().latitude) / 2, (arrayList_xunxian_Marker.get(j - 2).getPosition().longitude + arrayList_xunxian_Marker.get(j - 1).getPosition().longitude) / 2);
            MarkerOptions markerOptions_m = new MarkerOptions();
            markerOptions_m.position(point_m);
            markerOptions_m.draggable(true);
            markerOptions_m.anchor(0.5f, 0.5f);
            markerOptions_m.icon(BitmapDescriptorFactory.fromResource(R.drawable.add_18));
        }
        marker.showInfoWindow();
    }

    public List<Marker> get_arrayList_xunxian_Marker(){
        return arrayList_xunxian_Marker;
    }

    public void set_sata_land(boolean is_sata_land){
        if(is_sata_land)
            aMap.setMapType(AMap.MAP_TYPE_SATELLITE);// 卫星地图模式
//            switchGoogleSatelliteMap();
        else
            aMap.setMapType(AMap.MAP_TYPE_NORMAL);// 矢量地图模式
    }

    public void updateFlyZonesOnTheMap(final ArrayList<FlyZoneInformation> flyZones) {
        for (FlyZoneInformation flyZone : flyZones) {
            //print polygon
            if (flyZone.getSubFlyZones() != null) {
                SubFlyZoneInformation[] polygonItems = flyZone.getSubFlyZones();
                int itemSize = polygonItems.length;
                for (int i = 0; i != itemSize; ++i) {
                    if(polygonItems[i].getShape() == SubFlyZoneShape.POLYGON) {
                        DJILog.d("updateFlyZonesOnTheMap", "sub polygon points " + i + " size: " + polygonItems[i].getVertices().size());
                        DJILog.d("updateFlyZonesOnTheMap", "sub polygon points " + i + " category: " + flyZone.getCategory().value());
                        DJILog.d("updateFlyZonesOnTheMap", "sub polygon points " + i + " limit height: " + polygonItems[i].getMaxFlightHeight());
                        addPolygonMarker(polygonItems[i].getVertices(), flyZone.getCategory(), polygonItems[i].getMaxFlightHeight());
                    }
                    else if (polygonItems[i].getShape() == SubFlyZoneShape.CYLINDER){
                        LocationCoordinate2D tmpPos = polygonItems[i].getCenter();
                        double subRadius = polygonItems[i].getRadius();
                        DJILog.d("updateFlyZonesOnTheMap", "sub circle points " + i + " coordinate: " + tmpPos.getLatitude() + "," + tmpPos.getLongitude());
                        DJILog.d("updateFlyZonesOnTheMap", "sub circle points " + i + " radius: " + subRadius);

                        CircleOptions circle = new CircleOptions();
                        circle.radius(subRadius);
                        CoordinateConverter converter = new CoordinateConverter(context);
                        converter.from(CoordinateConverter.CoordType.GPS);
                        converter.coord(new LatLng(tmpPos.getLatitude(), tmpPos.getLongitude()));
// 执行转换操作
                        LatLng desLatLng = converter.convert();
                        circle.center(desLatLng);
                        switch (flyZone.getCategory()) {
                            case WARNING:
                                circle.strokeColor(Color.GREEN);
                                break;
                            case ENHANCED_WARNING:
                                circle.strokeColor(Color.BLUE);
                                break;
                            case AUTHORIZATION:
                                circle.strokeColor(Color.YELLOW);
                                unlockableIds.add(flyZone.getFlyZoneID());
                                break;
                            case RESTRICTED:
                                circle.strokeColor(Color.RED);
                                break;

                            default:
                                break;
                        }
                        aMap.addCircle(circle);
                    }
                }
            }
            else {
                CircleOptions circle = new CircleOptions();
                circle.radius(flyZone.getRadius());
                CoordinateConverter converter = new CoordinateConverter(context);
                converter.from(CoordinateConverter.CoordType.GPS);
                converter.coord(new LatLng(flyZone.getCoordinate().getLatitude(), flyZone.getCoordinate().getLongitude()));
// 执行转换操作
                LatLng desLatLng = converter.convert();
                circle.center(desLatLng);
                switch (flyZone.getCategory()) {
                    case WARNING:
                        circle.strokeColor(Color.GREEN);
                        break;
                    case ENHANCED_WARNING:
                        circle.strokeColor(Color.BLUE);
                        break;
                    case AUTHORIZATION:
                        circle.strokeColor(Color.YELLOW);
                        unlockableIds.add(flyZone.getFlyZoneID());
                        break;
                    case RESTRICTED:
                        circle.strokeColor(Color.RED);
                        break;

                    default:
                        break;
                }
                aMap.addCircle(circle);
            }
        }
    }

    private void addPolygonMarker(List<LocationCoordinate2D> polygonPoints, FlyZoneCategory flyZoneCategory, int height) {
        if (polygonPoints == null) {
            return;
        }

        ArrayList<LatLng> points = new ArrayList<>();

        for (LocationCoordinate2D point : polygonPoints) {
            CoordinateConverter converter = new CoordinateConverter(context);
            converter.from(CoordinateConverter.CoordType.GPS);
            converter.coord(new LatLng(point.getLatitude(), point.getLongitude()));
// 执行转换操作
            LatLng desLatLng = converter.convert();
            points.add(desLatLng);
        }
        int fillColor = getResources().getColor(R.color.limit_fill);
        if (painter.getHeightToColor().get(height) != null) {
            fillColor = painter.getHeightToColor().get(height);
        } else if (flyZoneCategory == FlyZoneCategory.AUTHORIZATION) {
            fillColor = getResources().getColor(R.color.auth_fill);
        } else if (flyZoneCategory == FlyZoneCategory.ENHANCED_WARNING || flyZoneCategory == FlyZoneCategory.WARNING) {
            fillColor = getResources().getColor(R.color.gs_home_fill);
        }
        Polygon plg = aMap.addPolygon(new PolygonOptions().addAll(points)
                .strokeColor(painter.getColorTransparent())
                .fillColor(fillColor));

    }
}
