package com.bn.ZDR.Util;

import android.annotation.SuppressLint;
import android.opengl.GLES30;

import com.bn.ZDR.View.MyGLSurfaceView;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

//加载后的物体——仅携带顶点信息，颜色随机
public class LoadedObjectVertexNormalTextureBullet
{
    int muMVPMatrixHandle;//总变换矩阵引用
    int muMMatrixHandle;//位置、旋转变换矩阵
    int maPositionHandle; //顶点位置属性引用
    int maNormalHandle; //顶点法向量属性引用
    int muLightDirectionHandle;//光源方向属性引用
    int maCameraHandle; //摄像机位置属性引用
    int maTexCoorHandle; //顶点纹理坐标属性引用
    String mVertexShader;//顶点着色器代码脚本
    String mFragmentShader;//片元着色器代码脚本

    int mProgramBullet;//自定义炸弹的渲染管线着色器程序id
    int muBzsj;//炸弹引爆时间

    FloatBuffer mVertexBuffer;//顶点坐标数据缓冲
    FloatBuffer mNormalBuffer;//顶点法向量数据缓冲
    FloatBuffer mTexCoorBuffer;//顶点纹理坐标数据缓冲
    int vCount=0;
    public LoadedObjectVertexNormalTextureBullet(MyGLSurfaceView mv, float[] vertices, float[] normals, float texCoors[])
    {
        //初始化顶点坐标与着色数据
        initVertexData(vertices,normals,texCoors);
        //初始化shader
        initShaderBullet(mv);
    }
    //初始化顶点坐标与着色数据的方法
    public void initVertexData(float[] vertices,float[] normals,float texCoors[])
    {
        //顶点坐标数据的初始化================begin============================
        vCount=vertices.length/3;

        //创建顶点坐标数据缓冲
        //vertices.length*4是因为一个整数四个字节
        ByteBuffer vbb = ByteBuffer.allocateDirect(vertices.length*4);
        vbb.order(ByteOrder.nativeOrder());//设置字节顺序
        mVertexBuffer = vbb.asFloatBuffer();//转换为Float型缓冲
        mVertexBuffer.put(vertices);//向缓冲区中放入顶点坐标数据
        mVertexBuffer.position(0);//设置缓冲区起始位置
        //特别提示：由于不同平台字节顺序不同数据单元不是字节的一定要经过ByteBuffer
        //转换，关键是要通过ByteOrder设置nativeOrder()，否则有可能会出问题
        //顶点坐标数据的初始化================end============================

        //顶点法向量数据的初始化================begin============================
        ByteBuffer cbb = ByteBuffer.allocateDirect(normals.length*4);
        cbb.order(ByteOrder.nativeOrder());//设置字节顺序
        mNormalBuffer = cbb.asFloatBuffer();//转换为Float型缓冲
        mNormalBuffer.put(normals);//向缓冲区中放入顶点法向量数据
        mNormalBuffer.position(0);//设置缓冲区起始位置
        //特别提示：由于不同平台字节顺序不同数据单元不是字节的一定要经过ByteBuffer
        //转换，关键是要通过ByteOrder设置nativeOrder()，否则有可能会出问题
        //顶点着色数据的初始化================end============================

        //顶点纹理坐标数据的初始化================begin============================
        ByteBuffer tbb = ByteBuffer.allocateDirect(texCoors.length*4);
        tbb.order(ByteOrder.nativeOrder());//设置字节顺序
        mTexCoorBuffer = tbb.asFloatBuffer();//转换为Float型缓冲
        mTexCoorBuffer.put(texCoors);//向缓冲区中放入顶点纹理坐标数据
        mTexCoorBuffer.position(0);//设置缓冲区起始位置
        //特别提示：由于不同平台字节顺序不同数据单元不是字节的一定要经过ByteBuffer
        //转换，关键是要通过ByteOrder设置nativeOrder()，否则有可能会出问题
        //顶点纹理坐标数据的初始化================end============================
    }


    //初始化shader
    @SuppressLint("NewApi")
    public void initShaderBullet(MyGLSurfaceView mv)
    {
        //加载顶点着色器的脚本内容
        mVertexShader=ShaderUtil.loadFromAssetsFile("vertex_simple_bullet.sh", mv.getResources());
        //加载片元着色器的脚本内容
        mFragmentShader=ShaderUtil.loadFromAssetsFile("frag_simple_bullet.sh", mv.getResources());
        //基于顶点着色器与片元着色器创建程序
        mProgramBullet = ShaderUtil.createProgram(mVertexShader, mFragmentShader);
        //获取程序中顶点位置属性引用
        maPositionHandle = GLES30.glGetAttribLocation(mProgramBullet, "aPosition");
        //获取程序中顶点颜色属性引用
        maNormalHandle= GLES30.glGetAttribLocation(mProgramBullet, "aNormal");
        //获取炸弹爆炸生命周期属性引用
        muBzsj=GLES30.glGetUniformLocation(mProgramBullet, "baozhashijian");
        //获取程序中总变换矩阵引用
        muMVPMatrixHandle = GLES30.glGetUniformLocation(mProgramBullet, "uMVPMatrix");
        //获取位置、旋转变换矩阵引用
        muMMatrixHandle = GLES30.glGetUniformLocation(mProgramBullet, "uMMatrix");
        //获取程序中光源位置引用
        muLightDirectionHandle=GLES30.glGetUniformLocation(mProgramBullet, "uLightDirection");
        //获取程序中顶点纹理坐标属性引用
        maTexCoorHandle= GLES30.glGetAttribLocation(mProgramBullet, "aTexCoor");
        //获取程序中摄像机位置引用
        maCameraHandle= GLES30.glGetUniformLocation(mProgramBullet, "uCamera");
    }

    public void drawSelf(int texId,float time)
    {
        //制定使用某套着色器程序
        GLES30.glUseProgram(mProgramBullet);
        //将最终变换矩阵传入着色器程序
        GLES30.glUniformMatrix4fv(muMVPMatrixHandle, 1, false, MatrixState3D.getFinalMatrix(), 0);
        //将爆炸时间传入shader程序
        GLES30.glUniform1f(muBzsj, time);
        //将位置、旋转变换矩阵传入着色器程序
        GLES30.glUniformMatrix4fv(muMMatrixHandle, 1, false, MatrixState3D.getMMatrix(), 0);
        //将光源位置传入渲染管线
        GLES30.glUniform3fv(muLightDirectionHandle, 1, MatrixState3D.lightDirectionFB);
        //将摄像机位置传入渲染管线
        GLES30.glUniform3fv(maCameraHandle, 1, MatrixState3D.cameraFB);

        // 将顶点位置数据传入渲染管线
        GLES30.glVertexAttribPointer
                (
                        maPositionHandle,
                        3,
                        GLES30.GL_FLOAT,
                        false,
                        3*4,
                        mVertexBuffer
                );
        //将顶点法向量数据传入渲染管线
        GLES30.glVertexAttribPointer
                (
                        maNormalHandle,
                        3,
                        GLES30.GL_FLOAT,
                        false,
                        3*4,
                        mNormalBuffer
                );
        //为画笔指定顶点纹理坐标数据
        GLES30.glVertexAttribPointer
                (
                        maTexCoorHandle,
                        2,
                        GLES30.GL_FLOAT,
                        false,
                        2*4,
                        mTexCoorBuffer
                );
        //启用顶点位置、法向量、纹理坐标数据
        GLES30.glEnableVertexAttribArray(maPositionHandle);
        GLES30.glEnableVertexAttribArray(maNormalHandle);
        GLES30.glEnableVertexAttribArray(maTexCoorHandle);
        //绑定纹理
        GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, texId);
        //绘制加载的物体S
        GLES30.glDrawArrays(GLES30.GL_TRIANGLES, 0, vCount);
    }
}